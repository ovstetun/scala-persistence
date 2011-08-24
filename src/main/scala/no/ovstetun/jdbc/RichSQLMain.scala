//////////////////////////////////////////////////////////////////////////////////////////
// RichSQL.scala
// @n8han after http://scala.sygneca.com/code/simplifying-jdbc
// @khrabrov package, de-;-ed

package no.ovstetun.jdbc

// In Scala we can specify multiple imports from the same package
// by combining them together.
import java.sql.{DriverManager, Connection, ResultSet, PreparedStatement, Statement, Date}
// We can also rename an import to avoid a name collision. Here
// we're changing the name of java.util.Random to JRandom.
import java.util.{Random => JRandom}
import DriverManager.{getConnection => connect}

// The underscore is used to import everything in a package or object.
// "Console" is a Scala singleton object we are bringing its public
// declarations into our namespace. Note that when we do this we are
// bringing in types, methods, classes -- everything.
import Console._

/** TestF2 demonstrates the use of the RichSQL object's facilities
to make JDBC work easier than Java. */
object RichSQLMain {

  // Bring in the RichSQL public declarations. Note that when
  // we import an implicit declaration, that implicit becomes
  // part of our namespace.
  import RichSQL._

  // a random number generator. Note that we're using our altered name.
  private val rnd = new JRandom()

  // commands to set up a simple database. Scala has multi-line
  // string constants, which are very handy when we want to embed
  // things like extended text, or sql commands.

  // was: id identity -- in H2, id serial -- in Pg
  private val setup = Array (
    """
        drop table if exists person
    ""","""
    create table person(
        id serial,
        type int,
        name varchar not null)
""")

  // some additional data. Scala's type inferencing makes this into an
  // Array[String] -- an Array of Strings.
  private val names =
    Array(
      "Ross",
      "Lynn",
      "John",
      "Terri",
      "Steve",
      "Zobeyda")

  def go = {
    // Create a connection to the database. Catching exceptions is
    // optional in Scala...we can put this in a try block or ignore
    // the exceptions and expect something "higher up" to catch them.
    // We imported DriverManager's connect method earlier, so we're
    // calling that here.
    // "conn" is also typed as implicit, which means it will
    // be automatically used in any function call that
    // requires a Connection in an implicit parameter position.
    implicit val conn = connect("jdbc:postgresql:database", "user", "pwd")

    // Our RichSQL environment does quite a bit for us
    // with minimal syntax. "setup" is an array of commands
    // to execute. Here we are triggering an implicit
    // conversion of "conn" to a RichConnection, which
    // is then passed an array of commands with the <<
    // operator. On RichConnection the << operator creates
    // a RichStatement object and passes the commands
    // to it for execution. Finally, our variable "s"
    // is typed as a statement, so an implicit conversion
    // from RichStatement to Statement is performed.
    // "s" is also typed as implicit, which means it will
    // be automatically used in any function call that
    // requires a Statement in an implicit parameter position.
    implicit val s: Statement = conn << setup

    // Creates a JDBC prepared statement. We can omit the
    // period and parentheses in simple calls x.f(y) can
    // be written as x f y.
    val insertPerson = conn prepareStatement "insert into person(type, name) values(?, ?)"
    // For each name in our list of names, load the prepared
    // statement with a random number and the name, then
    // execute it. Our RichPreparedStatement has type-specific
    // overloadings for the << operator which call the right
    // setXXX methods on the JDBC PreparedStatement. I've removed
    // the spaces between operators and identifiers here to
    // show that there are natural boundaries between operator characters
    // and identifiers. The <<! postfix operator calls the
    // PreparedStatement's execute method.
    for (val name <- names)
      insertPerson<<rnd.nextInt(10)<<name<<!

    // Execute a query against an implicit Statement. The
    // query function looks for a Statement declared as "implicit"
    // in this namespace and uses it automatically. We also
    // supply a construction function that builds a Person object
    // from a RichResultSet. There are a variety of implicit
    // conversions from RichResultSet to fundamental types. Each
    // invocation advances to the next column in the result set,
    // so Person(rs,rs,rs) makes three conversion calls. Since
    // Scala knows the types of the fields on Person, it knows
    // which implicit conversion functions to call.
    // "query" is producing a lazy sequence of results note that
    // the syntax for iterating over it is exactly the same as
    // those used earlier.
    // Once we've recovered our person object, we convert it to
    // XML and print it out.
    for (val person <- query("select * from person order by id", rs => Person(rs,rs,rs)))
      println(person.toXML)

    // The same thing can be done with a RichPreparedStatement
    for (val person <- "select * from person order by id" <<! (rs => Person(rs,rs,rs)))
      println(person.toXML)
  }

  /** A simple class holding information about a person. */
  case class Person(id: Long, tpe: Int, name: String) {
    def toXML = <person id={id.toString} type={tpe.toString}>{name}</person>
  }

  def main(args: Array[String]): Unit = {
//    val dbDriver = Class.forName("org.postgresql.Driver")
//    go
  }

  def p[X](x: X) = { println(x); x }
}