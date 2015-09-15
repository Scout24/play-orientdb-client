# Orient Play Client [![Build Status](https://travis-ci.org/ImmobilienScout24/play-orientdb-client.svg)](https://travis-ci.org/ImmobilienScout24/play-orientdb-client)
A play client to use the orient db http api.

# Create client
```scala
val config = OrientClientConfig(
    url = getConfigProperty("orientdb.url"),
    database = getConfigProperty("orientdb.database"),
    userName = getConfigProperty("orientdb.username"),
    password = getConfigProperty("orientdb.password")
)
// the WS Client is provided by the NingWSComponents trait
new OrientDbHttpClient(config, wsClient)
```

#Query
## Gremlin
```scala
import de.is24.play.orientdb.client.OrientDbHttpClient
import de.is24.play.orientdb.OrientStringContext._

val name = "Hugo"
val query = gremlin"""g.V("name", $name)"""

orientDbHttpClient.select[Person](query) // requires a Json.reads
```

##SQL
```scala
import de.is24.play.orientdb.client.OrientDbHttpClient
import de.is24.play.orientdb.OrientStringContext._

val name = "Hugo"
val query = sql"select from Person WHERE name = $name"

orientDbHttpClient.select[Person](query) // requires a Json.reads
```

##Batch
```scala
import de.is24.play.orientdb.Operation._

val createLockTableBatch = Seq(
      sql"Create class SchemaLock",
      sql"Create property SchemaLock.id String",
      sql"Create index idIndex on SchemaLock (id) UNIQUE",
      sql"Insert into SchemaLock set id = $lockId, lockedBy = null"
    ).asBatch()

createLockTableBatch.execute
```

##Transaction
```scala
Seq("INSERT INTO ...", "UPDATE ...").transactionally.execute
```

##Functions
```scala
orientClient.callFunction("testFunction")
orientClient.callFunction("testFunction", Map[String, Any]("a" -> "42", "b" -> 42, "c" -> false))
```
#Release

    sbt> release with-defaults
