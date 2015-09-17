import sbt._

trait Dependencies {

  val playVersion = "2.4.2"

  val specs2Version = "3.6.1"

  val testDependencies = Seq(
    "com.orientechnologies" % "orientdb-server" % "2.1.1",
    "com.orientechnologies" % "orientdb-graphdb" % "2.1.1",
    "com.tinkerpop.gremlin" % "gremlin-groovy" % "2.6.0",
    "org.mockito" % "mockito-core" % "1.10.19",
    "org.specs2" %% "specs2-core" % specs2Version,
    "org.specs2" %% "specs2-matcher" % specs2Version,
    "org.specs2" %% "specs2-junit" % specs2Version,
    "org.specs2" %% "specs2-mock" % specs2Version,
    "junit" % "junit" % "4.12",
    "com.novocode" % "junit-interface" % "0.11",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
    "com.typesafe.play" %% "play-test" % playVersion,
    "com.ning" % "async-http-client" % "1.9.29",
    "org.jboss.netty" % "netty" % "3.2.10.Final",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.14"
  ).map(_ % "test")

  val appDependencies = Seq(
    "com.typesafe.play" %% "play" % playVersion % "provided",
    "com.typesafe.play" %% "play-json" % playVersion % "provided",
    "org.apache.commons" % "commons-lang3" % "3.4",
    "com.jsuereth" %% "scala-arm" % "1.4",
    "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
    "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0",
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
    "de.heikoseeberger" %% "akka-http-play-json" % "1.1.0"
  )
}
