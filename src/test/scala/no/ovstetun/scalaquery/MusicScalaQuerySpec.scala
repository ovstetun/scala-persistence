package no.ovstetun
package scalaquery

import no.ovstetun.DBSupport
import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.extended.H2Driver
import org.specs2.execute.Result
import org.specs2.mutable.{Around, Specification}
import java.sql.Date
import org.scalaquery.ql.{Parameters, Join, Query}
import org.scalaquery.ql.Ordering.{NullsLast, Desc}

class MusicScalaQuerySpec extends Specification with DBSupport {
  lazy val db = Database.forDataSource(ds)
  implicit def conn = threadLocalSession.conn

  val s = new H2Driver with MusicDB
  import s.Implicit._
  import s._

  trait tdata extends Around {
    def around[T <% Result](t: => T) = db withSession {
      threadLocalSession withTransaction {
        loadData

        val res = t
        threadLocalSession.rollback()
        res
      }
    }
  }

  "scalaquery" should {
    "count artists" in new tdata {
      val i : Int = Query(Artists.count).first()
      i must_== 5
    }
    "find artist row by id" in new tdata {
      val q = Artists.createFinderBy(_.id)
      q(1001).firstOption must beSome
      q(1001).first must_== (1001, "Tool", "", Genre.Rock, date("1990-01-02"), None)

      q.first(1001) must_== (1001, "Tool", "", Genre.Rock, date("1990-01-02"), None)
      q.firstOption(1001) must beSome

      q.firstOption(999) must beNone
      q.first(999) must throwA[NoSuchElementException]
    }
    "insert single artist" in new tdata {
      val i = Artists.i.insert(("Seigmen", "", Genre.Rock, date("1989-12-27"), Some(date("2008-06-22"))))
      i must_== 1
    }
    "insert a batch of artists" in new tdata {
      val artists = List(
        ("Seigmen", "", Genre.Rock, date("1989-12-27"), Some(date("2008-06-22"))),
        ("Muse", "", Genre.Rock, date("1994-06-01"), None),
        ("Oslo Ess", "", Genre.Rock, date("2010-06-01"), None)
      )
      val i = Artists.i.insertAll(artists :_*)
      i must beSome(3)
    }
    "map duration column to Duration case class" in new tdata {
      val q = for (s <- Songs if s.id === 1001) yield s.x
      q.first must_== (1001, "Vicarious", Duration(7,6))

      val q2 = Songs.createFinderBy(_.id)
      q2.first(1001) must_== (1001, "Vicarious", Duration(7,6), 1, 1004)
    }
    "find total length of album from a query with mapping to Duration" in new tdata {
      val q = for (s <- Songs if s.album_id === 1004) yield s.duration.sum
      q.first must_== Some(Duration(75, 45))
    }
    "map rating for album" in new tdata {
      val q = Albums.createFinderBy(_.id)
      q.first(1004) must_== (1004, "10000 Days", date("2006-05-02"), None, 1001)
      q.first(1003) must_== (1003, "Lateralus", date("2001-05-15"), Some(Six), 1001)
    }
    "insert an album with case object as rating" in new tdata {
      Albums.i.insert("lala", date("2006-05-02"), Some(Six), 1001) must_== 1
      Albums.i.insert("lala2", date("2006-05-02"), None, 1001) must_== 1
      Albums.i.insert("lala3", date("2006-05-02"), None, 1001) must_== 1

      val q = Query(Albums.count)
      q.first must_== 27

      val q2 = Albums.filter(_.rating === (Six:Rating))
      val l2 = q2.list()

      l2.size must_== 2

      val q3 = Albums.filter(_.rating isNull) //=== (null:Rating))
      val l3 = q3.list
      l3.size must_== 25

    }
    "query for persons with None" in new tdata {
      val q = Persons.filter(_.biography isNull) //=== (null:Option[String]))
      val l = q.list
      l.size must_== 22
    }
    "Find rockers" in new tdata {
      val q = for {a <- Artists if a.maingenre === Genre.Rock} yield a.id ~ a.name
      q.list must_== List((1001, "Tool"), (1005, "A Perfect Circle"))
    }
    "find rockers with albumcount" in new tdata {
//      val q = for {a <- Artists
//                   al <- Albums.where(_.artist_id === a.id)
//                   c <- Query(al.id.count)
//                   _ <- Query groupBy(a.id)
//                   if a.maingenre === Genre.Rock} yield a.id ~ a.name

//      val q = for {al <- Albums
//                   a <- Artists
//                   c <- Query(al.id.count)
//                   _ <- Query groupBy(a.id) if a.maingenre === Genre.Rock} yield a.id ~ a.name ~ c
      val q = for {
        Join(a, al) <- Artists leftJoin Albums on (_.id === _.artist_id)
        c <- Query(al.id.count)
        _ <- Query groupBy(a.id)
        _ <- Query orderBy(Desc(c))
        if a.maingenre === Genre.Rock
      } yield a.id ~ a.name ~ c

      val l = q.list
      l must_== List((1001, "Tool", 4), (1005, "A Perfect Circle", 0))

      val q2 = q.take(2).drop(1)
      q2.list must_== List((1005, "A Perfect Circle", 0))

      val q3 = q.take(1)
      q3.list must_== List((1001, "Tool", 4))
    }
    "find artists with albums" in new tdata {
      val q = for {
        a <- Artists
        al <- Albums
        if a.id === al.artist_id
      } yield a.id ~ a.name
      q.list.toSet must_== Set((1001, "Tool"), (1002, "Pink Floyd"), (1003, "Arcade Fire"))

      val q2 = for {
        a <- Artists
        if a.id in (for (al <- Albums) yield al.artist_id)
      } yield a.id ~ a.name
      q2.list must_== List((1001, "Tool"), (1002, "Pink Floyd"), (1003, "Arcade Fire"))
    }
    "find artist as a function by name as parameter" in new tdata {
      def byName(n:String) = for (a <- Artists if a.name === n.bind) yield a.id ~ a.name
      byName("Tool").first must_== (1001, "Tool")
      byName("Pink Floyd").first must_== (1002, "Pink Floyd")
      byName("don't think so").first must throwA[NoSuchElementException]

      byName("Tool").firstOption must beSome((1001, "Tool"))
      byName("nope").firstOption must beNone
    }
    "find artist as a value by name as parameter" in new tdata {
      val qArtist = for {
        n <- Parameters[String]
        a <- Artists if a.name === n
      } yield a.id ~ a.name

      qArtist("Tool").first must_== (1001, "Tool")
      qArtist("lala").firstOption must beNone
    }
    "find all artists with more than 4 albums" in new tdata {
      val q = for {
        al <- Albums
        a  <- al.artist
        c  <- Query(al.id.count)
        _  <- Query groupBy(a.id)
        _  <- Query having(_ => c > 4)
      } yield a.id ~ a.name

      q.list.size must_== 1
      q.first must_== (1002, "Pink Floyd")
    }
    "find artists without albums" in new tdata {
      val q = for {
        a <- Artists
        if a.id notIn (for (al <- Albums) yield al.artist_id)
      } yield a.id ~ a.name
      q.list.size must_== 2
      q.list must_== List((1004,"Jay-Z"), (1005, "A Perfect Circle"))
    }
    "find all persons with more than one artist" in new tdata {
      val q = for {
        pa <- PersonsArtists
        a  <- pa.artist
        p  <- pa.person
        n  <- Query(a.id.count)
        _  <- Query groupBy(p.id)
        _  <- Query having(_ => n > 1)
      } yield p.id ~ p.firstname ~ p.lastname

      q.list must_== List((1001, "Maynard James", "Keenan"))
    }
    "find length of albums" in new tdata {
      val byId = for {
        id <- Parameters[Int]
        Join(al, s) <- Albums leftJoin Songs on(_.id === _.album_id)
        len <- Query(s.duration.sum)
        _ <- Query groupBy(al.id)
//        _ <- Query orderBy(Desc(len))
        if (al.id === id)
      } yield al.id ~ len

      byId.first(1003) must_== (1003, Some(Duration(78,51)))
    }
  }

  implicit def date(dateStr : String) : Date = {
    Date.valueOf(dateStr)
  }
}
