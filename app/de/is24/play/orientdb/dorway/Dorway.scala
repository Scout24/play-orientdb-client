package de.is24.play.orientdb.dorway

import java.net.InetAddress
import java.time.Instant
import java.util.Base64

import com.google.common.reflect.ClassPath
import de.is24.play.orientdb.Operation._
import de.is24.play.orientdb.client.{OrientDbHttpClient, OrientProtocol}
import OrientProtocol._
import de.is24.play.orientdb.OrientSqlContext._
import de.is24.play.orientdb._
import org.slf4j.LoggerFactory
import play.api.libs.ws.WSResponse
import resource.managed

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.util.control.NonFatal

class Dorway(orientDbHttpClient: OrientDbHttpClient)(implicit executionContext: ExecutionContext) {

  val log = LoggerFactory.getLogger(classOf[Dorway])

  private implicit val orientClient = orientDbHttpClient
  private val base64Encoder = Base64.getEncoder

  private val schemaVersionTable = "SchemaVersion"
  val lockId = "did"

  private val hostName = InetAddress.getLocalHost.getHostName

  def migrate(): Future[Unit] = {
    lockDb()
    for {
      version <- getDatabaseSchemaVersion
      _ <- updateDatabaseSchema(version)
      _ <- unlockDb()
    } yield ()
  }

  def getDatabaseSchemaVersion: Future[Int] = {
    isSchemaClassExistent.flatMap { schemaExists =>
      if(schemaExists)
        selectCurrentSchemaVersion
      else
        createSchemaClass.map(_ => 0)
    }
  }

  private def isSchemaClassExistent: Future[Boolean] = {
    val query = sql"select count(*) from (select expand(classes) from metadata:schema) where name = $schemaVersionTable"
    orientDbHttpClient.select[CountResult](query)
      .map {_.headOption.getOrElse(throw new IllegalStateException("Cant determine database schema version"))}
      .map(_.count > 0)
  }

  private def selectCurrentSchemaVersion: Future[Int] = {
    orientDbHttpClient.select[MaxResult](sql"select max(version) from SchemaVersion")
      .map(_.headOption.getOrElse(throw new IllegalStateException("Cant determine database schema version")))
      .map(_.max.toInt)
  }

  private def createSchemaClass: Future[WSResponse] =
    orientDbHttpClient.command(sql"Create class SchemaVersion")

  def updateDatabaseSchema(currentVersion: Int): Future[Unit] = {
    val allMigrations: List[Migration] = loadMigrationsFromPath("orient/migration")
    val migrationsToApply = allMigrations.filter(_.version > currentVersion)
    migrationsToApply.foldLeft(Future.successful(())) { (lastMigrationResult, nextMigration) =>
        lastMigrationResult
          .flatMap { _ => applyMigration(nextMigration)}
    }
  }

  private def applyMigration(migration: Migration): Future[Unit] = {
    log.info(s"Applying migration ${migration.version}: ${migration.script}".stripLineEnd)
    orientDbHttpClient.command(OrientDbQuery(migration.script))
      .flatMap { _ =>
        val command = sql"insert into SchemaVersion set version = ${migration.version}, script = ${migration.script}, timestamp = ${Instant.now}"
        orientDbHttpClient.command(command)
      }
      .map(_ => ())
  }

  def loadMigrationsFromPath(path: String): List[Migration] = {
    val classPath = ClassPath.from(getClass.getClassLoader)
    val resources = classPath.getResources.asScala.toSeq
    log.info("Loading migrations")
    val versionsAndContents = resources
      .filter(_.getResourceName startsWith (path + "/"))
      .map { r => r.getResourceName.substring(r.getResourceName.lastIndexOf("/") + 1) }
      .map { r => r.split("__", 2)(0).toInt -> r }
      .map {
      case (version, file) =>
        val content = managed(getClass.getClassLoader.getResource(s"$path/$file").openStream()).acquireAndGet { stream =>
          Source.fromInputStream(stream).mkString
        }
        Migration(version, content)
    }
    val versions = versionsAndContents.map(_.version)
    val uniqueVersions = versions.distinct

    val nonUniqueVersions = versions.diff(uniqueVersions)

    if (nonUniqueVersions.nonEmpty) throw new IllegalArgumentException(s"Found non-unique versions in migration versions: ${nonUniqueVersions.mkString(", ")}")
    versionsAndContents.toList.sortBy(_.version)
  }

  @tailrec
  final def lockDb(): Unit = {
    val createLockTableBatch = Seq(
      sql"Create class SchemaLock",
      sql"Create property SchemaLock.id String",
      sql"Create index idIndex on SchemaLock (id) UNIQUE",
      sql"Insert into SchemaLock set id = $lockId, lockedBy = null"
    ).asBatch()

    val lockSuccess = Await.result(createLockTableBatch.execute
      .recover {
      case NonFatal(e) =>
        log.info("Lock table probably already exists.", e)
    }.flatMap { _ =>
      sql"Update SchemaLock set lockedBy = $hostName where (lockedBy is null or lockedBy = $hostName) and id=$lockId".transactionally.execute
    }.map { lockResponse =>
      log.debug("{}", lockResponse)
      (lockResponse \ "result").as[Seq[UpdateResult]].headOption.map(u => (0L +: u.value).max).getOrElse(0) == 1
    }, 10.seconds)
    if (lockSuccess)
      ()
    else {
      log.warn("Failed to aquire de.is24.play.orientdb.dorway lock. Will retry in 10 seconds.")
      Thread.sleep(10.seconds.toMillis)
      lockDb()
    }
  }

  def unlockDb(): Future[Unit] = {
    log.info("Unlocking de.is24.play.orientdb.dorway")
    sql"Update SchemaLock set lockedBy = null where lockedBy = $hostName and id=$lockId"
      .transactionally
      .execute
      .map{response =>
        log.debug(response.toString())
        ()
      }
  }
}
