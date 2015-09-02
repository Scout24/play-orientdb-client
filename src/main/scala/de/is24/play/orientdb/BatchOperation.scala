package de.is24.play.orientdb

case class BatchOperation(transaction: Boolean, operations: Seq[Operation]) {
  def execute(implicit orientDbHttpClient: OrientDbHttpClient) = {
    orientDbHttpClient.executeBatch(this)
  }
}

