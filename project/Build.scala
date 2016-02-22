import sbtrelease.ReleasePlugin.autoImport._
import sbt.Keys._
import sbt._

import com.typesafe.sbt.SbtPgp.autoImportImpl._
import sbtrelease.ReleaseStateTransformations._

import com.typesafe.sbt.SbtScalariform.autoImport._
import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

object Build extends Build with Dependencies {

  val javaVersion = "1.8"
  val encoding = "utf-8"

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


}
