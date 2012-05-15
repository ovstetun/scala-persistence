# Scala Persistence

This project is to show different ways to interact with a relational database in Scala.

* Raw JDBC
* JPA (using [Eclipselink][eclipselink])
* [ScalaQuery][scalaquery]
* [Squeryl][squeryl]


## Running the project

The project contains specifications written in [specs2], and built with [sbt 0.10.x][sbt]. The specs require a file database generated by [Liquibase][liquibase] to work. This is to make sure all the strategies work against the same database. To run the tests:

    sbt update
    sbt copy-resources
    ./db.sh
    sbt test
    
If by some weird error the database becomes corrupt, just delete all the `test.*.db` files: `rm test.*.db`.


[specs2]:http://specs2.org
[sbt]:https://github.com/harrah/xsbt/
[scalaquery]:http://scalaquery.org
[squeryl]:http://squeryl.org
[eclipselink]:http://www.eclipse.org/eclipselink/
[liquibase]:http://www.liquibase.org/
