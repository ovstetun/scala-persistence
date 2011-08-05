package no.ovstetun.jpa

import org.specs2.mutable.Specification
import javax.persistence.Persistence
import org.specs2.mutable.After

class JPASpec extends Specification {
  lazy val emf = Persistence.createEntityManagerFactory("sakPU")

  trait t extends After {
    lazy val em = emf.createEntityManager()
    em.getTransaction.begin

    def after = {
      em.getTransaction.rollback
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

      em.persist(u)
      q.getSingleResult must_== 1
    }
  }
  
  step {
    emf.close
    success
  }
}