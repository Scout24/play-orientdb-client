package orient

import de.is24.play.orientdb.{DeleteOperation, Operation}
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import de.is24.play.orientdb.OrientProtocol._

class OperationSpec extends Specification {

  "A operation" should {
    "be converted to JSON" in {
      val operation: Operation = DeleteOperation(Map("name" -> "test"))
      Json.stringify(Json.toJson(operation)) must be equalTo """{"record":{"name":"test"},"type":"d"}"""
    }
  }

}
