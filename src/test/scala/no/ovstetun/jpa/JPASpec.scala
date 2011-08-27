package no.ovstetun.jpa

import no.ovstetun.DBSupport

class JPASpec extends BaseJPASpec with DBSupport {
  "jpa" should {
    "be able to count" in new t {
      loadData

      val q = em.createQuery("select count(u) from User u ")
      q.getSingleResult must_== 4
    }
    "be able to insert" in new t {
      val q = em.createQuery("select count(u) from User u ")
      q.getFirstResult must_== 0

      val u = new User
      u.firstname = "tm"
      u.lastname = "o"
      u.id must_== 0

      em.persist(u)
      em.flush
      u.id must_!= 0

      q.getSingleResult must_== 1
    }
  }
}
