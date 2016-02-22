package de.is24.play.orientdb

import de.is24.play.orientdb.OrientStringContext._
import org.specs2.mutable.Specification

class OrientSqlContextSpec extends Specification {

  "A orients sql context" should {
    "escape sql strings" in {
      val id = 1
      val evil = """' delete from user; """""""
      val query =
        sql"""
             SELECT * FROM DUAL where id = $id and userName = $evil and 1=1"""
      query.query must be equalTo """SELECT * FROM DUAL where id = 1 and userName = "' delete from user; \"\"\"\"" and 1=1"""
      query.language must be equalTo "sql"
    }

    "escape gremlin strings" in {
      val someValue = "123"
      val query = gremlin"""g.V("someKey", $someValue)"""

      query.query must be equalTo """g.V("someKey", "123")"""
      query.language must be equalTo "gremlin"
    }
  }

}
