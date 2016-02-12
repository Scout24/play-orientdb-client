package de.is24.play.orientdb

import scala.language.implicitConversions

sealed trait Operation

case class UpdateOperation(record: Map[String, String]) extends Operation

case class DeleteOperation(record: Map[String, String]) extends Operation

case class CreateOperation(record: Map[String, String]) extends Operation

case class CommandOperation(language: String, command: String) extends Operation

case class ScriptOperation(language: String, script: Seq[String]) extends Operation

object Operation {
  def sqlScript(queries: String*): ScriptOperation = new ScriptOperation(language = "sql", script = queries.toSeq)

  def sqlCommand(query: String): CommandOperation = new CommandOperation(language = "sql", command = query)


  class Batchable(queries: Seq[String]) {
    def asBatch(transaction: Boolean = false): BatchOperation = {
      new BatchOperation(transaction = transaction, operations = Seq(
        Operation.sqlScript(queries: _*)
      ))
    }

    def transactionally: BatchOperation = asBatch(transaction = true)
  }

  implicit def stringToBatchable(query: String): Batchable = new Batchable(Seq(query))

  implicit def seqToBatchable(queries: Seq[String]): Batchable = new Batchable(queries)

  implicit def queryToBatchable(query: OrientDbQuery): Batchable = new Batchable(Seq(query.query))

  implicit def queriesToBatchable(queries: Seq[OrientDbQuery]): Batchable = new Batchable(queries.map(_.query))
}
