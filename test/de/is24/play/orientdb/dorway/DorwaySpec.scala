package de.is24.play.orientdb.dorway

import com.google.common.reflect.ClassPath
import de.is24.play.orientdb.client.OrientDbHttpClient
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }

import scala.concurrent.ExecutionContext.Implicits.global

class DorwaySpec extends Specification with Mockito with FutureAwaits with DefaultAwaitTimeout {

  "A de.is24.play.orientdb.dorway" should {
    "load migrations from path" in new WithDorway {
      val migrations = dorway.loadOrderedMigrationsFromPath(getClass.getClassLoader, "orient/migration")

      migrations must contain(Migration(1, "create class Test"))
      migrations must contain(Migration(2, "create class FooBar"))
    }

    "throw when migrations versions are not unique" in new WithDorway {
      dorway.loadOrderedMigrationsFromPath(getClass.getClassLoader, "orient/testmigration_with_two_v1") must throwA[IllegalArgumentException].like {
        case e: IllegalArgumentException => e.getMessage must beEqualTo("Found non-unique versions in migration versions: 1")
      }
    }
  }

  private trait WithDorway extends Scope {

    val client: OrientDbHttpClient = mock[OrientDbHttpClient]
    val dorway = new Dorway(client)
  }

}
