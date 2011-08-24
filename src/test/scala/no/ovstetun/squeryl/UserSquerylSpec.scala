package no.ovstetun
package squeryl

import org.specs2.mutable.Specification
import org.squeryl.adapters.H2Adapter
import org.squeryl.{Schema, KeyedEntity, Session, SessionFactory}
import org.specs2.specification.AroundExample
import org.specs2.execute.Result

class UserSquerylSpec extends Specification with DBSupport with AroundExample {

  SessionFactory.concreteFactory = Some(() =>
    Session.create(ds.getConnection, new H2Adapter)
  )

  import org.squeryl.PrimitiveTypeMode._
  def around[T <% Result](t: => T) = transaction {
    val res = t

    Session.currentSession.connection.rollback

    res
  }

  import SquerylSchema._

  "Users" should {
    "be insertable" in {
      val tm = users.insert(new User("Trond", "Ovstetun"))
      tm.id must_!= 0
    }
    "be able to count the contents" in {
      val q:Long = from(users)(_ =>
        compute(count)
      )

      q must_== 0
    }
    "calculate the average id" in {
      val q:Option[Float] = from(users)(u =>
        compute(avg(u.id))
      )

      q must beNone
    }
    "select all users" in {
      val u1 = users.insert(new User("Trond", "Ovstetun"))
      val u2 = users.insert(new User("Trond", "Ovstetun"))
      val all = List(u1, u2)

      val us = from(users)(select(_))

      us.foreach(u =>
        u.id must beGreaterThan(0)
      )
      val l = for (u <- us) yield {
        u.id must beGreaterThan(0)
        u
      }
      
      us.size must_== 2
      all must_== l
    }
    "create user" in {
      val tm = new User("trond", "ovstetun")
      tm.isPersisted must_== false
      tm.id must_== 0

      val saved = save(tm)
      saved.isPersisted must_== true
      saved.id must_!= 0
      saved must beTheSameAs(tm)
      
      tm.id must_!= 0
      tm.isPersisted must_== true
    }
    "delete user" in {
      users.insert(new User("Trond", "Ovstetun"))
      users.deleteWhere(u => u.id != 0) must_== 0
    }
  }

}
