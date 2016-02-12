package de.is24.play.orientdb

import de.is24.play.orientdb.client.OrientDbHttpClient
import play.api.libs.json.JsValue
import scala.concurrent.Future

case class BatchOperation(transaction: Boolean, operations: Seq[Operation]) {
  def execute(implicit orientDbHttpClient: OrientDbHttpClient): Future[JsValue] = {
    orientDbHttpClient.executeBatch(this)
  }
}

