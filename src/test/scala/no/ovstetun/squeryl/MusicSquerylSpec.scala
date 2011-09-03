package no.ovstetun
package squeryl

import no.ovstetun.DBSupport
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter

import org.squeryl.PrimitiveTypeMode._
import org.specs2.mutable.{Around, Specification}
import org.specs2.execute.Result
import java.util.Date
import java.text.SimpleDateFormat

class MusicSquerylSpec extends Specification with DBSupport {

  SessionFactory.concreteFactory = Some(() =>
    Session.create(ds.getConnection, new H2Adapter)
  )
  trait tdata extends Around {
    def around[T <% Result](t: => T) = inTransaction {
      loadData(Session.currentSession.connection)
      val res = t

      Session.currentSession.connection.rollback()

      res
    }
  }

  import MusicSquerylSchema._


  "MusicSchema with Squeryl" should {
    "insert artist" in new tdata {
      val seigmen = new Artist("Seigmen", "", Genre.Rock, "1989-12-27", Some("2008-06-22"))
//      seigmen.save
      artists.insert(seigmen)

      seigmen.id must_!= 0
    }
    "count artists" in new tdata {
      val q = from(artists)(_ => compute(count))
      val c:Long = q
      c must_== 5
    }
    "retrieve artist by id" in new tdata {
      val missing = artists.lookup(999)
      missing must beNone

      val tool = artists.lookup(1001)
      tool must beSome
      tool.map {t =>
        t.id must_== 1001
        t.name must_== "Tool"
        t.maingenre must_== Genre.Rock
      }
    }
    "retrieve all artists" in new tdata {
//      val l:Iterable[Artist] = artists
//      l.size must_== 5
      artists.size must_== 5

//      val all:Iterable[Artist] = artists.where(true)
//      all.m
    }
    "find rockers" in new tdata {
      val q = from(artists)(a => where(a.maingenre === Genre.Rock) select(a))
      for (a <- q) {
        a.id must beGreaterThan(1000)
      }
      q.size must_== 2
    }
    "find rockers with albumcount" in new tdata {
      val q = from(artists, albums)((a, al) =>
        where(al.artist_id === a.id)
        groupBy(a.id) compute(a.id, a.name, count(al.id))
      )

      q.size must_== 3
      //      for ((i:Int, name:String, c:Long) <- q) {
      //        i must beGreaterThan(1000)
      //        c must_== 1
      //      }
      //      q.head must_== (1001, "Tool", 4)
    }
    "find all artists with albums" in pending
    "find all artists with more than 5 albums" in pending
    "find all persons with more than one album" in pending
    "length of albums" in pending
    "associate a new album with an artist" in new tdata {
      val neon = new Album("Neon Bible", "2007-03-06", None, 0)
      val suburbs = new Album("The Suburbs", "2010-05-02", None, 0)
      for (af <- artists.lookup(1003)) {
        af.albums.associate(neon)
        af.albums.assign(suburbs)
      }
      neon.artist_id must_== 1003
      neon.isPersisted must beTrue

      suburbs.artist_id must_== 1003
      suburbs.isPersisted must beFalse

      albums.insert(suburbs)
      suburbs.isPersisted must beTrue
    }
  }

  implicit def str2date(dateStr:String) : Date = {
    new SimpleDateFormat("yyyy-mm-dd").parse(dateStr);
  }
}
