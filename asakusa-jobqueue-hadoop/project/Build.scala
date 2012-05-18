import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "asakusa-jobqueue-hadoop"
  val appVersion = "0.2-SNAPSHOT"

  val appDependencies = Seq(
    "com.asakusafw" %% "asakusa-jobqueue-core" % appVersion,
    "com.github.play2war" %% "play2-war-core" % "0.3",
    "com.typesafe.akka" % "akka-testkit" % "2.0" % "test")

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers ++= Seq(
      "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/"))

}
