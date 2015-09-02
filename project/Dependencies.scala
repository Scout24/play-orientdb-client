import sbt._

trait Dependencies {

  val playVersion = "2.4.2"

  val specs2Version = "3.6.1"

  val testDependencies = Seq(
    "com.orientechnologies" % "orientdb-server" % "2.1.0",
    "com.orientechnologies" % "orientdb-enterprise" % "2.1.0",
    "org.mockito" % "mockito-core" % "1.10.19" ,
    "org.specs2" %% "specs2-core" % specs2Version,
    "org.specs2" %% "specs2-matcher" % specs2Version,
    "org.specs2" %% "specs2-junit" % specs2Version,
    "junit" % "junit" % "4.12",
    "com.novocode" % "junit-interface" % "0.11",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
    "com.typesafe.play" %% "play-test" % playVersion,
    "com.ning" % "async-http-client" % "1.9.29"
  ).map(_ % "test")

  val appDependencies = Seq(
    "com.typesafe.play" %% "play" % playVersion % "provided",
    "com.typesafe.play" %% "play-ws" % playVersion % "provided",
    "org.apache.commons" % "commons-lang3" % "3.4",
    "com.jsuereth" %% "scala-arm" % "1.4"
  )
}
