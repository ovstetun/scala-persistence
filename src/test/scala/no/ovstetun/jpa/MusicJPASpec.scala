package no.ovstetun
package jpa

import java.text.SimpleDateFormat
import java.util.Date

class MusicJPASpec extends BaseJPASpec with DBSupport {
  "Music database using JPA" should {
    "count empty table" in new t {
      val q = em.createQuery("SELECT COUNT(a) FROM Artist a", classOf[Long])
      q.getSingleResult must_== 0
    }
    "assign an id to persisted artist on flush" in new t {
      val a = new Artist
      a.name = "Pink Floyd"
      a.biography = "bla bla bla"
      a.founded = "1965-01-20"

      em.persist(a)
      em.flush()
      a.id must_!= 0
    }
    "retrieve all artists" in new t {
      loadData

      val q = em.createQuery("SELECT a FROM Artist a", classOf[Artist])
      val artists = q.getResultList
      artists.size must_== 4
    }
    "retrieve Tool with correct genre" in new t {
      loadData
      val tool = em.find(classOf[Artist], 1001)
      tool.name must_== "Tool"
      tool.maingenre must_== Genre.Rock
    }
  }

  implicit def str2date(dateStr : String) : Date = {
    new SimpleDateFormat("yyyy-mm-dd").parse(dateStr);
  }
}
