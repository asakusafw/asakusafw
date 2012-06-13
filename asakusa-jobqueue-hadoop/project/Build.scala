import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "asakusa-jobqueue-hadoop"
  val appVersion = "0.4-SNAPSHOT"

  val appDependencies = Seq(
    "com.asakusafw" %% "asakusa-jobqueue-core" % appVersion,
    "com.github.play2war" %% "play2-war-core" % "0.3",
    "com.typesafe.akka" % "akka-testkit" % "2.0" % "test")

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(

    organization := "com.asakusafw",

    description := "Asakusa JobQueue Server for Hadoop Jobs",

    homepage := Some(url("http://asakusafw.com")),

    startYear := Some(2012),

    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

    organizationName := "Asakusa Framework Team",

    organizationHomepage := Some(url("http://asakusafw.com")),

    resolvers ++= Seq(
      "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/",
      "Asakusa Framework Repository" at "http://asakusafw.s3.amazonaws.com/maven/releases",
      "Asakusa Framework Snapshot Repository" at "http://asakusafw.s3.amazonaws.com/maven/snapshots"))

  val assembly = TaskKey[File]("assembly", "Assemble dist files")

}
