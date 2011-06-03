package no.ovstetun

import org.specs2.mutable.Specification
import org.scalaquery.session.Database
import Database.threadLocalSession

import org.scalaquery.ql.extended.H2Driver
import org.scalaquery.ql.Query

class UserSpec extends Specification {
  val driver = H2Driver
  import driver.Implicit._

  lazy val db = Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")
  lazy val session = db.createSession()

  val s = new H2Driver with Schema
  import s._

  "Users" should {
    "allow insert on " in {
      db withSession {
        Users.ddl.create
        Query(Users.count).first must_== 0

        val i = Users.forinsert.insert(("trond", "ovstetun"))
        i must_== 1
        Query(Users.count).first must_== 1
      }
    }
  }

}