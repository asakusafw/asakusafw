war := null

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0.1",
  "ch.qos.logback" % "logback-core" % "1.0.0",
  "ch.qos.logback" % "logback-classic" % "1.0.0")

// for test
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.8.2" % "test",
  "org.specs2" %% "specs2" % "1.7.1" % "test",
  "com.typesafe.akka" % "akka-testkit" % "2.0.1" % "test")
