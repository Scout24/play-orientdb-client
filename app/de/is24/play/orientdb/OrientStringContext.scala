package de.is24.play.orientdb

import org.apache.commons.lang3.StringEscapeUtils

import scala.language.implicitConversions

class OrientStringContext(stringContext: StringContext) {

  def gremlin(args: Any*): OrientDbQuery = {
    interpolated("gremlin", args: _*)
  }

  def sql(args: Any*): OrientDbQuery = {
    interpolated("sql", args: _*)
  }

  private def interpolated(language: String, args: Any*): OrientDbQuery = {
    val strings = stringContext.parts.iterator
    val expressions = args.iterator

    val queryBuilder = new StringBuilder()

    for (string <- strings) {
      queryBuilder.append(string)
      if (expressions.hasNext) {
        val escapedExpression = expressions.next() match {
          case x: Number => x
          case s => "\"" + StringEscapeUtils.escapeJava(s.toString) + "\""
        }
        queryBuilder.append(escapedExpression)
      }
    }

    OrientDbQuery(
      query = queryBuilder.toString().trim,
      language = language)
  }

}

object OrientStringContext {
  implicit def toOrientStringContext(stringContext: StringContext): OrientStringContext = new OrientStringContext(stringContext)
}
