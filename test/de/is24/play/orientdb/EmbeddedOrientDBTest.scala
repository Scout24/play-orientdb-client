package de.is24.play.orientdb

import de.is24.play.orientdb.testsupport.OrientDBScope
import org.specs2.mutable.Specification
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import Operation._

class EmbeddedOrientDBTest extends Specification with FutureAwaits with DefaultAwaitTimeout {

  sequential

  "An embedded server" should {

    "reach the server page" in new OrientDBScope {
      ok
    }

    "be able to perform dorway migration multiple times" in new OrientDBScope {
      executeDorway()

      await(dorway.getDatabaseSchemaVersion) must beEqualTo(2)
    }
    "be able to execute batch commands" in new OrientDBScope {
      val batchOperation = BatchOperation(transaction = true, operations = Seq(CommandOperation(language = "sql", command = "SELECT 1")))
      await(orientClient.executeBatch(batchOperation))

      implicit val client = orientClient
      await(Seq("SELECT 1", "SELECT 2").transactionally.execute)

      await("INSERT INTO V SET notice = 'inserted transactionally'".transactionally.execute)
    }
  }

}
