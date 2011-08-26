# Scala Persistence

This project is to show different ways to interact with a relational database in Scala.

* Raw JDBC
* JPA (using [Eclipselink][eclipselink])
* [ScalaQuery][scalaquery]
* [Squeryl][squeryl]


## Running the project

The project contains specifications written in [specs2], and built with [sbt 0.10.x][sbt]

    xsbt test


[specs2]:http://specs2.org
[sbt]:https://github.com/harrah/xsbt/
[scalaquery]:http://scalaquery.org
[squeryl]:http://squeryl.org
[eclipselink]:http://www.eclipse.org/eclipselink/
