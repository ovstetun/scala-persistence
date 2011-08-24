name := "Scala Persistence"

version := "1.0-SNAPSHOT"

organization := "no.ovstetun"

libraryDependencies += "org.scalaquery" % "scalaquery_2.9.0" % "0.9.4"

libraryDependencies += "org.squeryl" %% "squeryl" % "0.9.4"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.5" % "test",
  "com.h2database" % "h2" % "1.3.153" % "test",
  "org.liquibase" % "liquibase-core" % "2.0.1" % "test"
)

libraryDependencies ++= Seq(
  "org.hibernate" % "hibernate-entitymanager" % "3.6.1.Final",
  "org.scala-libs" %% "scalajpa" % "1.4"
)

scalaVersion := "2.9.0-1"

resolvers += ScalaToolsSnapshots

parallelExecution in Test := false
