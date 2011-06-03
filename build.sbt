name := "Scala Persistence"

version := "1.0-SNAPSHOT"

organization := "no.ovstetun"

libraryDependencies += "org.scalaquery" %% "scalaquery" % "0.9.4"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.3" % "test",
  "com.h2database" % "h2" % "1.3.153" % "test"
)

scalaVersion := "2.9.0"
