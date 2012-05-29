import java.io.FileInputStream
import java.util.jar.Manifest
import org.codehaus.plexus.archiver.tar.TarArchiver

war <<= (war, name, version, scalaVersion) map { (war, name, version, scalaVersion) =>
  IO.withTemporaryDirectory { dir =>
    val files = IO.unzip(war, dir)
    val metainf = new File(dir, "META-INF")
    val manifest = new File(metainf, "MANIFEST.MF")
    val versionfile = new File(metainf, name + ".version")
    IO.writeLines(versionfile, Seq(name + "_" + scalaVersion + "-" + version))
    val manifestin = new FileInputStream(manifest)
    try {
      IO.jar((files + versionfile - manifest).map{ file =>
        file -> file.getAbsolutePath.substring(dir.getAbsolutePath.length+1) }, war, new Manifest(manifestin))
    }
    finally {
      manifestin.close
    }
  }
  war
}

assembly <<= (name, version, target, war) map { (name, version, target, war) =>
  val archiver = new TarArchiver()
  archiver.setDestFile(target / ("asakusa-jobqueue-server-" + version + ".tar.gz"))
  val compressionMethod = new TarArchiver.TarCompressionMethod()
  compressionMethod.setValue("gzip")
  archiver.setCompression(compressionMethod)
  archiver.addFile(war, "webapps/jobqueue.war")
  archiver.addDirectory(file("asakusa"), "")
  archiver.createArchive()
  archiver.getDestFile()
}

addArtifact(Artifact(appName, "war", "war"), war)

addArtifact(Artifact(appName, "tgz", "tar.gz"), assembly)
