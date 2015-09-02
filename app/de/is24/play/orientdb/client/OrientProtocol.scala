package de.is24.play.orientdb.client

import de.is24.play.orientdb._
import play.api.libs.functional.syntax._
import play.api.libs.json._

object OrientProtocol {
  implicit val countFormat = Json.format[CountResult]
  implicit val updateWrites = Json.writes[UpdateResult]
  implicit val maxFormat = Json.format[MaxResult]

  implicit val updateRead = (JsPath \ "value").read[Seq[Long]].map(value => UpdateResult(value)) or
    (JsPath \ "value").read[Long].map(value => UpdateResult(Seq(value)))

  implicit val operationWrites = new Writes[Operation] {

    implicit val scriptOperationFormat = Json.format[ScriptOperation]
    implicit val commandOperationFormat = Json.format[CommandOperation]
    implicit val createOperationFormat = Json.format[CreateOperation]
    implicit val deleteOperationFormat = Json.format[DeleteOperation]
    implicit val updateOperationFormat = Json.format[UpdateOperation]

    override def writes(o: Operation): JsValue = {
      val (operationType, operationJson) = o match {
        case scriptOperation: ScriptOperation =>
          "script" -> scriptOperationFormat.writes(scriptOperation)
        case updateOperation: UpdateOperation =>
          "u" -> updateOperationFormat.writes(updateOperation)
        case deleteOperation: DeleteOperation =>
          "d" -> deleteOperationFormat.writes(deleteOperation)
        case createOperation: CreateOperation =>
          "c" -> createOperationFormat.writes(createOperation)
        case commandOperation: CommandOperation =>
          "cmd" -> commandOperationFormat.writes(commandOperation)
        case _ =>
          throw new UnsupportedOperationException("operation is not yet supported")
      }
      operationJson.asInstanceOf[JsObject] + ("type" -> JsString(operationType))
    }
  }

  implicit val batchOperationWrites = Json.writes[BatchOperation]
}
