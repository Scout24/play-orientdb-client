package de.is24.play.orientdb.testsupport

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Path, Paths }
import java.util.regex.{ Matcher, Pattern }

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.orientechnologies.orient.graph.gremlin.OGremlinHelper
import com.orientechnologies.orient.server.{ OServer, OServerMain }
import de.is24.play.orientdb.client.{ OrientClientConfig, OrientDbHttpClient }
import de.is24.play.orientdb.dorway.Dorway
import org.slf4j.bridge.SLF4JBridgeHandler
import org.specs2.mutable.After
import org.specs2.specification.Scope
import org.specs2.specification.mutable.SpecificationFeatures
import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object OrientDBScope {
  val actorSystem = ActorSystem()
}

trait OrientDBScope extends Scope with After with FutureAwaits with DefaultAwaitTimeout with SpecificationFeatures {

  lazy val orientConfig = OrientClientConfig(
    url      = "http://localhost:2480",
    database = "temp",
    userName = "root",
    password = "root"
  )

  lazy val orientClient = new OrientDbHttpClient(orientConfig)(OrientDBScope.actorSystem)

  lazy val dorway: Dorway = new Dorway(
    orientDbHttpClient = orientClient
  )

  startUp()

  private[this] object server {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    val orientServer: OServer = OServerMain.create
    val dbConfig = loadDbConfig
    orientServer.startup(dbConfig)
    orientServer.activate()

    def shutdown(): Unit = orientServer.shutdown()

    def networkInterface: InetSocketAddress = orientServer.getNetworkListeners.toList.head.getInboundAddr

    private def loadDbConfig: String = {
      val configFile: Path = Paths.get(getClass.getResource("/embedded-orientdb.xml").toURI)
      new String(
        Files.readAllBytes(configFile),
        StandardCharsets.UTF_8
      ).replaceAll(Pattern.quote("@@@dbname@@@"), Matcher.quoteReplacement(orientConfig.database))
    }
  }

  def startUp(): Unit = {
    server.networkInterface
    OGremlinHelper.global().create()
    await(orientClient.createDatabase())
    executeDorway()
  }

  override def after: Unit = {
    server.shutdown()
  }

  def executeDorway(): Unit = {

    Await.result(dorway.migrate(getClass.getClassLoader, "orient/migration"), 60.seconds)
  }
}
