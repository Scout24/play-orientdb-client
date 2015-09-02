package embeddedorient

import de.is24.play.orientdb.{CommandOperation, BatchOperation}
import org.specs2.mutable.Specification
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

class EmbeddedOrientDBTest extends Specification with FutureAwaits with DefaultAwaitTimeout {

  sequential

  "An embedded server" should {

    "reach the server page" in new OrientDBScope {
      ok
    }

//    "be able to perform dorway migration multiple times" in new OrientDBScope {
//      executeDorway()
//
//      await(dorway.getDatabaseSchemaVersion) must beEqualTo(47)
//    }
//    "be able to execute batch commands" in new OrientDBScope {
//      val batchOperation = BatchOperation(transaction = true, operations = Seq(CommandOperation(language = "sql", command = "SELECT 1")))
//      await(orientClient.executeBatch(batchOperation))
//
//    }
  }

}
