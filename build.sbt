name := "Scala Persistence"

version := "1.0-SNAPSHOT"

organization := "no.ovstetun"

libraryDependencies += "org.scalaquery" % "scalaquery_2.9.0" % "0.9.4" withSources()

libraryDependencies += "org.squeryl" %% "squeryl" % "0.9.4" withSources()

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.4" % "test" withSources(),
  "com.h2database" % "h2" % "1.3.153" % "test" withSources(),
  "org.liquibase" % "liquibase-core" % "2.0.1" % "test" withSources()
)

scalaVersion := "2.9.0-1"

resolvers += ScalaToolsSnapshots

parallelExecution in Test := false
