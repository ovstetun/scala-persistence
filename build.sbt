name := "Scala Persistence"

version := "1.0-SNAPSHOT"

organization := "no.ovstetun"

libraryDependencies += "org.scalaquery" %% "scalaquery" % "0.10.0-M1"

libraryDependencies += "org.squeryl" %% "squeryl" % "0.9.4"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.9" % "test",
  "com.h2database" % "h2" % "1.3.153" % "test",
  "org.liquibase" % "liquibase-core" % "2.0.1" % "test",
  "org.dbunit" % "dbunit" % "2.4.8" % "test"
)

libraryDependencies ++= Seq(
  "org.eclipse.persistence" % "eclipselink" % "2.3.0",
  "org.eclipse.persistence" % "javax.persistence" % "2.0.3",
  "ch.qos.logback" % "logback-classic" % "1.0.3",
  "org.scala-libs" %% "scalajpa" % "1.4"
)

scalaVersion := "2.9.1"

resolvers += ScalaToolsSnapshots

resolvers += "eclipselink" at "http://download.eclipse.org/rt/eclipselink/maven.repo/"

parallelExecution in Test := false
