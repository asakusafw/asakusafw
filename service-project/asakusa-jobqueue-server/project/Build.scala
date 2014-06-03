import sbt._
import Keys._
import PlayProject._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import de.johoop.findbugs4sbt.FindBugs._

import org.codehaus.plexus.archiver.tar.TarArchiver

object JobQueueBuildSettings {

  val appName = "asakusa-jobqueue"
  val appVersion = "0.7-SNAPSHOT"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.asakusafw",
    version := appVersion,

    description := "Asakusa JobQueue Server",
    homepage := Some(url("http://asakusafw.com")),
    startYear := Some(2012),

    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

    organizationName := "Asakusa Framework Team",
    organizationHomepage := Some(url("http://asakusafw.com")),

    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/",
      "Asakusa Framework Repository" at "http://asakusafw.s3.amazonaws.com/maven/releases",
      "Asakusa Framework Snapshot Repository" at "http://asakusafw.s3.amazonaws.com/maven/snapshots"),

    testOptions in Test += Tests.Argument("console", "junitxml"),

    EclipseKeys.withSource := true
  ) ++ findbugsSettings
}

object JobQueueBuild extends Build {

  import JobQueueBuildSettings._
  import Commands._

  lazy val server = Project(
    appName + "-server",
    file("."),
    settings = buildSettings ++ Seq(
      publishArtifact := false,
      assembly <<= assemblyTask)
  ) aggregate(core, hadoop)

  lazy val core = Project(
    appName + "-core",
    file("core"),
    settings = buildSettings ++ findbugsSettings
  )

  lazy val hadoop = PlayProject(
    appName + "-hadoop",
    appVersion,
    path = file("hadoop"),
    mainLang = SCALA,
    settings = buildSettings ++ findbugsSettings
  ) dependsOn(core)

  object Commands {
    val assembly = TaskKey[File]("assembly", "Assemble dist files")
    val assemblyTask = (name, version, target, packagedArtifacts in hadoop in Compile) map { (name, version, target, artifacts) =>
      val archiver = new TarArchiver()
      archiver.setDestFile(target / (name + "-" + version + ".tar.gz"))

      val compressionMethod = new TarArchiver.TarCompressionMethod()
      compressionMethod.setValue("gzip")
      archiver.setCompression(compressionMethod)

      archiver.addFile(artifacts(Artifact(hadoop.id, "war", "war")), "webapps/jobqueue.war")
      archiver.addDirectory(file("src/main/dist"), "")

      archiver.createArchive()
      archiver.getDestFile()
    }
  }

}
