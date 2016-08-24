package de.is24.play.orientdb

import de.is24.play.orientdb.testsupport.OrientDBScope
import org.specs2.mutable.Specification
import play.api.libs.json.JsValue
import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }
import Operation._
import OrientStringContext._

class EmbeddedOrientDBTest extends Specification with FutureAwaits with DefaultAwaitTimeout {

  sequential

  "An embedded server" should {

    "reach the server page" in new OrientDBScope {
      ok
    }

    "be able to perform dorway migration multiple times" in new OrientDBScope {
      executeDorway()

      await(dorway.getDatabaseSchemaVersionOrCreateIt) must beEqualTo(2)
    }
    "be able to execute batch commands" in new OrientDBScope {
      val batchOperation = BatchOperation(transaction = true, operations = Seq(CommandOperation(language = "sql", command = "SELECT 1")))
      await(orientClient.executeBatch(batchOperation))

      implicit val client = orientClient
      await(Seq("SELECT 1", "SELECT 2").transactionally.execute)

      await("INSERT INTO V SET notice = 'inserted transactionally'".transactionally.execute)
    }

    "be able to execute commands that return no content" in new OrientDBScope {
      val batchOperation = BatchOperation(transaction = true, operations = Seq(CommandOperation(language = "sql", command = "SELECT 1")))
      await(orientClient.executeBatch(batchOperation))

      implicit val client = orientClient
      await(Seq("CREATE class Blubb extends V", "CREATE property Blubb.name string").asBatch().execute)
      await(orientClient.command(sql"alter property Blubb.name mandatory true")) must not beNull
    }

    "be able to select json with sql" in new OrientDBScope {
      implicit val client = orientClient
      await("INSERT INTO V set name = 'johnny'".asBatch().execute)

      val selected = await(orientClient.selectJson(sql"SELECT FROM V"))

      selected must haveSize(1)
      (selected.head \ "name").as[String] must beEqualTo("johnny")
    }

    "be able to select json with gremlin" in new OrientDBScope {
      implicit val client = orientClient
      await("INSERT INTO V set name = 'johnny'".asBatch().execute)

      val selected = await(orientClient.selectJson(gremlin"g.V()"))

      selected must haveSize(1)
      (selected.head \ "name").as[String] must beEqualTo("johnny")
    }

  }

}
