import sbt.Keys._
import sbt._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

val javaVersion = "1.8"
val encoding = "utf-8"
val playVersion = "2.5.4"
val specs2Version = "3.7.2"
val orientVersion = "2.2.4"

val testDependencies = Seq(
  "com.orientechnologies" % "orientdb-server" % orientVersion,
  "com.orientechnologies" % "orientdb-graphdb" % orientVersion,
  "com.tinkerpop.gremlin" % "gremlin-groovy" % "2.6.0",
  "org.mockito" % "mockito-core" % "1.10.19",
  "org.specs2" %% "specs2-core" % specs2Version,
  "org.specs2" %% "specs2-matcher" % specs2Version,
  "org.specs2" %% "specs2-junit" % specs2Version,
  "org.specs2" %% "specs2-mock" % specs2Version,
  "junit" % "junit" % "4.12",
  "com.novocode" % "junit-interface" % "0.11",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.4",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
  "com.ning" % "async-http-client" % "1.9.29",
  "org.jboss.netty" % "netty" % "3.2.10.Final",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.14"
).map(_ % "test")

val appDependencies = Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided",
  "com.typesafe.play" %% "play-json" % playVersion % "provided",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "com.jsuereth" %% "scala-arm" % "1.4",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.7",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.7.0"
)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

lazy val root = Project(
  id = "root",
  base = file("."),
  settings = Defaults.coreDefaultSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ defaultScalariformSettings)
  .settings(
    name := "play-orientdb-client",
    organization := "de.is24",
    scalaVersion := "2.11.7",
    ivyScala := ivyScala.value map {
      _.copy(overrideScalaVersion = true)
    },
    libraryDependencies ++= appDependencies ++ testDependencies,
    javacOptions ++= Seq("-source", javaVersion, "-target", javaVersion, "-Xlint"),
    scalacOptions ++= Seq("-feature", "-language:postfixOps", "-target:jvm-" + javaVersion, "-unchecked", "-deprecation", "-encoding", encoding),
    compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
    (compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle,
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
    ),
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    /**
      * scalariform
      */
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(RewriteArrowSymbols, false)
      .setPreference(AlignArguments, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(SpacesAroundMultiImports, true)
      .setPreference(AlignParameters, true),

    /**
      * publish
      */
    pgpReadOnly := false,
    pomExtra in Global := {
      <url>https://github.com/ImmobilienScout24/play-orientdb-client</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          </license>
        </licenses>
        <scm>
          <connection>scm:git:github.com/ImmobilienScout24/play-orientdb-client</connection>
          <developerConnection>scm:git:git@github.com:ImmobilienScout24/play-orientdb-client</developerConnection>
          <url>github.com/ImmobilienScout24/play-orientdb-client</url>
        </scm>
        <developers>
          <developer>
            <id>is24-phaun</id>
            <name>Patrick Haun</name>
            <url>http://github.com/bomgar</url>
          </developer>
        </developers>
    },
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommand("publishSigned"),
      releaseStepCommand("sonatypeRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )


  ).enablePlugins(play.sbt.PlayScala)
