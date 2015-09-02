package de.is24.play.orientdb

import de.is24.play.orientdb.client.OrientDbHttpClient

case class BatchOperation(transaction: Boolean, operations: Seq[Operation]) {
  def execute(implicit orientDbHttpClient: OrientDbHttpClient) = {
    orientDbHttpClient.executeBatch(this)
  }
}

