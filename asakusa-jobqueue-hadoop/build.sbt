import java.io.FileInputStream
import java.util.jar.Manifest

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
