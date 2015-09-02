import sbt._

trait Dependencies {

  val playVersion = "2.4.2"

  val slickVersion = "3.0.0"
  val macwireVersion = "1.0.1"
  val specs2Version = "3.6.1"
  val dropwizardMetricsVersion = "3.1.2"

  val testDependencies = Seq(
    "com.orientechnologies" % "orientdb-server" % "2.1.0",
    "org.mockito" % "mockito-core" % "1.10.19" ,
    "org.specs2" %% "specs2-core" % specs2Version,
    "org.specs2" %% "specs2-matcher" % specs2Version,
    "org.specs2" %% "specs2-junit" % specs2Version,
    "junit" % "junit" % "4.12",
    "com.novocode" % "junit-interface" % "0.11",
    "com.h2database" % "h2" % "1.4.185",
    "org.fluentlenium" % "fluentlenium-core" % "0.10.3",
    "org.fluentlenium" % "fluentlenium-festassert" % "0.10.3",
    "org.seleniumhq.selenium" % "selenium-java" % "2.46.0",
    "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.46.0",
    "org.apache.commons" % "commons-exec" % "1.3",  // This dependency seems to be missing in 2.46.0. Check if this still necessary when upgrading Selenium.
    "com.softwaremill.macwire" %% "macros" % macwireVersion,
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-codegen" % slickVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.flywaydb" % "flyway-core" % "3.2.1",
    "mysql" % "mysql-connector-java" % "5.1.34",
    "com.zaxxer" % "HikariCP" % "2.3.9",
    "de.is24.sso" % "client-shared-cookie-lib" % "1.146",
    "javax.servlet" % "javax.servlet-api" % "3.1.0",
    "org.webjars" %% "webjars-play" % "2.4.0-1",
    "org.webjars" % "bootstrap" % "3.1.1-2",
    "de.is24.deviceidentification" % "did-web-client" % "1.0.4",
    "com.jsuereth" %% "scala-arm" % "1.4",
    "is24-play-common" %% "is24-play-common" % "2.0.2",
    "is24-slick-common" %% "is24-slick-common" % "3.0.0",
    "com.typesafe.play" %% "play-specs2" % "2.4.2",
    "com.jsuereth" %% "scala-arm" % "1.4",
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
