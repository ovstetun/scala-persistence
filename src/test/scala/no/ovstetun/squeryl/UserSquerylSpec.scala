package no.ovstetun
package squeryl

import org.specs2.mutable.Specification
import org.squeryl.adapters.H2Adapter
import org.squeryl.{Schema, KeyedEntity, Session, SessionFactory}
import org.specs2.specification.AroundExample
import org.specs2.execute.Result

case class User(val id:Int, var firstname:String, var lastname:String) extends KeyedEntity[Int] {

}
object SquerylSchema extends Schema {
  val users = table[User]("users")
}

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
      val tm = users.insert(new User(0, "Trond", "Ovstetun"))
      tm.id must_!= 0
    }
    "be able to count the contents" in {
      val q:Long = from(users)(_ =>
        compute(count)
      )

      q must_== 0
    }
  }

}
