organization := "com.asakusafw"

name := "asakusa-jobqueue-core"

version := "0.4-SNAPSHOT"

description := "Asakusa JobQueue Core"

homepage := Some(url("http://asakusafw.com"))

startYear := Some(2012)

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

organizationName := "Asakusa Framework Team"

organizationHomepage := Some(url("http://asakusafw.com"))

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Asakusa Framework Repository" at "http://asakusafw.s3.amazonaws.com/maven/releases",
  "Asakusa Framework Snapshot Repository" at "http://asakusafw.s3.amazonaws.com/maven/snapshots")

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0",
  "ch.qos.logback" % "logback-core" % "1.0.0",
  "ch.qos.logback" % "logback-classic" % "1.0.0")

// for test
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.8.2" % "test",
  "org.specs2" %% "specs2" % "1.7.1" % "test",
  "com.typesafe.akka" % "akka-testkit" % "2.0" % "test")

testOptions in Test += Tests.Argument("console", "junitxml")
