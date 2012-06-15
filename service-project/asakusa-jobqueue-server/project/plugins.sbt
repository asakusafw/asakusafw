// The Typesafe repository 
resolvers ++= Seq(
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/")

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.0.1")

// play2war plugin
addSbtPlugin("com.github.play2war" % "play2-war-plugin" % "0.4")

// sbteclipse
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.0.0")

// findbugs4sbt
addSbtPlugin("de.johoop" % "findbugs4sbt" % "1.1.6")
