package de.is24.play.orientdb

import org.apache.commons.lang3.StringEscapeUtils

import scala.language.implicitConversions

class OrientSqlContext(stringContext: StringContext) {

  private var escapedExpression: Any = _

  def sql(args: Any*): OrientDbQuery = {
    val strings = stringContext.parts.iterator
    val expressions = args.iterator

    val sqlBuilder = new StringBuilder()

    for (string <- strings) {
      sqlBuilder.append(string)
      if (expressions.hasNext) {
        escapedExpression = expressions.next() match {
          case x: Number => x
          case s => "\"" + StringEscapeUtils.escapeJava(s.toString) + "\""
        }
        sqlBuilder.append(escapedExpression)
      }
    }

    OrientDbQuery(sqlBuilder.toString())
  }

}

object OrientSqlContext {
  implicit def toOrientSqlContext(stringContext: StringContext): OrientSqlContext = new OrientSqlContext(stringContext)
}
