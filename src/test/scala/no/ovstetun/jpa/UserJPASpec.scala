package no.ovstetun
package jpa

class UserJPASpec extends BaseJPASpec with DBSupport {
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
      u.email = "tmo@ovstetun.no"
      u.id must_== 0

      em.persist(u)
      em.flush()
      
      u.id must_!= 0

      q.getSingleResult must_== 1
    }
  }
}
