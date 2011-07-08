package no.ovstetun
package scalaquery

import org.scalaquery.session.Database
import Database.threadLocalSession
import org.scalaquery.ql.extended.H2Driver
import org.scalaquery.ql.Query

import org.specs2.mutable._
import org.specs2.execute._
import org.specs2.specification._

class UserSpec extends Specification with AroundExample with DBSupport {
  lazy val db = Database.forDataSource(ds)

  val s = new H2Driver with Schema
  import s.Implicit._
  import s._

  def around[T <% Result](t: => T) = db withSession {
    threadLocalSession withTransaction {
      val res = t
      threadLocalSession.rollback()
      res
    }
  }

  "Users" should {
    "be able to count" in {
      Query(Users.count).first must_== 0
    }
    "allow insert of user in empty structure" in {
      val i = Users.forinsert.insert(("trond", "ovstetun"))
      i must_== 1
      Query(Users.count).first must_== 1
    }
    "allow retrieval" in {
      pending
    }
  }
}
