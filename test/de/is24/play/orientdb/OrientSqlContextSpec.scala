package de.is24.play.orientdb

import de.is24.play.orientdb.OrientSqlContext._
import org.specs2.mutable.Specification

class OrientSqlContextSpec extends Specification {

  "A orients sql context" should {
    "escape strings" in {
      val id = 1
      val evil = """' delete from user; """""""
      val query =
        sql"""
             SELECT * FROM DUAL where id = $id and userName = $evil and 1=1"""
      query.query must be equalTo """SELECT * FROM DUAL where id = 1 and userName = "' delete from user; \"\"\"\"" and 1=1"""
    }
  }

}
