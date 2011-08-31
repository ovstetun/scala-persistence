package no.ovstetun
package jpa

import java.text.SimpleDateFormat
import java.util.Date

class MusicJPASpec extends BaseJPASpec with DBSupport {
  trait tdata extends t {
    loadData
  }

  "Music database using JPA" should {
    "run a count for a full table" in new tdata {
      val q = em.createQuery("SELECT COUNT(a) FROM Artist a", classOf[Long])
      q.getSingleResult must_== 5
    }
    "assign an id to persisted artist on flush" in new t {
      val a = new Artist
      a.name = "Pink Floyd"
      a.biography = "bla bla bla"
      a.founded = "1965-01-20"
      a.maingenre = Genre.Alternative

      em.persist(a)
      em.flush()
      a.id must_!= 0
    }
    "retrieve Tool with correct genre by id" in new tdata {
      val tool = em.find(classOf[Artist], 1001)
      tool.name must_== "Tool"
      tool.maingenre must_== Genre.Rock
    }
    "perform an update" in new tdata {
      val tool = em.find(classOf[Artist], 1001)
      tool.name must_== "Tool"
      tool.name = "Updated..."

      // emulate the ending of a transaction
      em.flush()
      em.clear()

      val updated = em.find(classOf[Artist], 1001)
      updated.name must_== "Updated..."
      updated must not beTheSameAs(tool)
    }
    "retrieve Jay-Z using a query by name" in new tdata {
      val jzq = em.createQuery("SELECT a FROM Artist a WHERE a.name = :name", classOf[Artist])
      jzq.setParameter("name", "Jay-Z")

      val jz = jzq.getSingleResult
      jz.id must_== 1004
      jz.maingenre must_== Genre.Rap
      jz.persons.size() must_== 1
    }
    "find by id returns null and Artist with its albums and persons" in new tdata {
      em.find(classOf[Artist], 999) must_== null
      var tool :Artist = em.find(classOf[Artist], 1001)
      tool.albums.size() must_== 4
      tool.persons.size() must_== 4
    }
    "retrieve all artists is java list" in new tdata {
      val q = em.createQuery("SELECT a FROM Artist a", classOf[Artist])
      val artists : java.util.List[Artist] = q.getResultList
      artists.size must_== 5

      import scala.collection.JavaConversions._
      val scalaArtists = q.getResultList
      artists.count(_ => true) must_== 5

      val (rockers, others) = artists.partition(_.maingenre == Genre.Rock)
      rockers.size must_== 2
      others.size must_== 3
    }
    "count all albums" in new tdata {
      em.createQuery("SELECT count(a) FROM Album a", classOf[Long]).getSingleResult must_== 24
    }
  }
  "Music database using ScalaEntityManager (RichJPA)" should {
    "count artists" in new tdata {
      val q = RichEM.createQuery[Long]("SELECT COUNT(a) FROM Artist a")
      q.findOne must_== Some(5)
      q.getSingleResult() must_== 5
    }
    "find by id returns None and Some using scalajpa" in new tdata {
      RichEM.find(classOf[Artist], 999) must beNone
      RichEM.find(classOf[Artist], 1001) must beSome[Artist]
      RichEM.find(classOf[Artist], 1002) must beSome[Artist]
    }
    "retrieve all artists is a scala collection" in new tdata {
      val q = RichEM.createQuery[Artist]("SELECT a FROM Artist a")
      val artists = q.getResultList()
      artists.count(_ => true) must_== 5

      val (rockers, others) = artists.partition(_.maingenre == Genre.Rock)
      rockers.size must_== 2
      others.size must_== 3
    }
    "find by name is nicer" in new tdata {
      val q = RichEM.createQuery[Artist]("SELECT a FROM Artist a WHERE a.name = :name")
      q.setParams("name" -> "Jay-Z")
      val jz = q.getSingleResult()
      jz.id must_== 1004

      q.findAll must contain(jz)
      q.findOne must_== Some(jz)
    }
  }
  "aggregate queries and such" should {
    "find artists with more than four albums" in new tdata {
      val q = em.createQuery("SELECT a FROM Artist a WHERE size(a.albums) > 4", classOf[Artist])
      val artists = q.getResultList
      artists.size must_== 1
    }
    "find artists without albums" in new tdata {
      val q_size = em.createQuery("SELECT a FROM Artist a WHERE size(a.albums) = 0", classOf[Artist])
      q_size.getResultList.size must_== 2

      val q_empty = em.createQuery("SELECT a FROM Artist a WHERE a.albums IS EMPTY", classOf[Artist])
      q_empty.getResultList.size must_== 2
    }
    "find all Rock bands using Genre.Rock Scala Enum" in new tdata {
      val q = RichEM.createQuery[Artist]("SELECT a FROM Artist a WHERE a.maingenre = :genre")
      q.setParams("genre" -> Genre.Rock)
      
      q.getResultList.size must_== 2
    }
    "find persons involved in more than one band" in new tdata {
      val q = RichEM.createQuery[Person]("SELECT p FROM Person p WHERE size(p.artists) > 1")
      q.findAll.size must_== 1

      val mjk = RichEM.find(classOf[Person], 1001)

      q.findOne must_== mjk
    }
    "find total length of album from a query" in new tdata {
      val q = RichEM.createQuery[Long]("SELECT sum(s.duration) FROM Album a JOIN a.songs s WHERE a.id = :album")
      q.setParams("album" -> 1004)
      q.findOne must_== Some(4545)
      q.getSingleResult must_== 4545L
    }
    "find total length of album by traversal" in new tdata {
      var aenima = em.find(classOf[Album], 1004)
      aenima.duration must_== 4545
    }
    "order albums according to length" in new tdata {
      val q = RichEM.createQuery[(Artist, Long)](
        """
        SELECT a, sum(s.duration)
        FROM Album a JOIN a.songs s
        GROUP BY a
        ORDER BY sum(s.duration) DESC
        """)
      val all = q.findAll
      all.size must_== 2

      val q2 = RichEM.createQuery[(Artist, Long)](
        """
        SELECT a, sum(s.duration)
        FROM Album a LEFT JOIN a.songs s
        GROUP BY a
        ORDER BY sum(s.duration) DESC
        """)
      val all2 = q2.findAll
      all2.size must_== 24
    }
  }

  implicit def str2date(dateStr : String) : Date = {
    new SimpleDateFormat("yyyy-mm-dd").parse(dateStr);
  }
}
