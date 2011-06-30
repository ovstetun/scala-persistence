package no.ovstetun.scalaquery

import org.scalaquery.session.Database
import Database.threadLocalSession
import org.scalaquery.ql.extended.H2Driver
import org.scalaquery.ql.Query

import org.specs2.mutable._
import org.specs2.execute._
import org.specs2.specification._

class UserSpec extends Specification with AroundExample {
  val driver = H2Driver
  import driver.Implicit._

  lazy val db = Database.forURL("jdbc:h2:test", driver = "org.h2.Driver", user = "sa", password = "")

  val s : Schema = new H2Driver with Schema
  import s._

  def around[T <% Result](t: => T) = db withSession {
    threadLocalSession withTransaction {
      val res = t

      threadLocalSession.rollback()

      res
    }
  }

  "Users" should {
    "allow insert of user in empty structure" in {
      Query(Users.count).first must_== 0

      val i = Users.forinsert.insert(("trond", "ovstetun"))
      i must_== 1
      Query(Users.count).first must_== 1
    }
    "allow retrieval" in {
      pending
    }
  }
}
