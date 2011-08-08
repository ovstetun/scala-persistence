package no.ovstetun.jpa

import org.specs2.mutable.Specification
import org.specs2.mutable.After
import javax.persistence.Persistence

class JPASpec extends Specification {
  lazy val emf = Persistence.createEntityManagerFactory("pu")

  trait t extends After {
    val em = emf.createEntityManager()
    em.getTransaction.begin()

    def after {
      em.getTransaction.rollback()
    }
  }

  "jpa" should {
    "be able to count" in new t {
      val q = em.createQuery("select count(u) from User u ")
      q.getSingleResult must_== 0
    }
    "be able to insert" in new t {
      val q = em.createQuery("select count(u) from User u ")
      q.getFirstResult must_== 0

      val u = new User
      u.firstname = "tm"
      u.lastname = "o"
      u.id must_== 0

      em.persist(u)
      u.id must_!= 0

      q.getSingleResult must_== 1
    }
  }
  
  step {
    emf.close()
    success
  }
}
