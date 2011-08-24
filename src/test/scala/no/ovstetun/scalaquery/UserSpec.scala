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

  trait withData extends Scope {
    val us = List(("trond", "ovstetun"),
              ("christian", "ihle"),
              ("svein", "melby"))
    Users.forinsert.insertAll(us :_*)
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
    "allow insert of a bunch of users" in {
      val i = Users.forinsert.insertAll(
        ("trond", "ovstetun"),
        ("thomas", "pettersen")
      )
      i must_== Some(2)
    }
    "make a query using a for comprehension" in {
      val q = for (u <- Users if u.firstname === "Trond") yield u

      val l:List[(Int, String, String, Option[String])] = q.list
      l.size must_== 0
    }
    "map query into a case class" in new withData {
      val q = for(u <- Users) yield u
      val l:List[User]= q.mapResult({case (a,b,c,d) => User(a,b,c)}).list
      l.size must_== 3

      val q2 = for(u <- q) yield u.a
      val l2 : List[User] = q2.list
      l2.size must_== 3

      val q3 = for (u <- Users) yield u.a
      val l3:List[User] = q3.list
      l3.size must_== 3

      l must_== l2
      l must_== l3
    }
    "map values to class" in new withData {
      val q = for(u <- Users) yield u.a
      for (u:User <- q) {
        u.id should be_>(1)
      }
    }
  }
}
