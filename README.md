#Query
```
val query = sql"select count(*) from (select expand(classes) from metadata:schema) where name = $schemaVersionTable"
orientDbHttpClient.select[CountResult](query)
```
#Release

    sbt> release with-defaults
