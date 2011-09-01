package no.ovstetun
package scalaquery

import no.ovstetun.DBSupport
import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.extended.H2Driver
import org.specs2.execute.Result
import org.specs2.mutable.{Around, Specification}
import org.scalaquery.ql.Query
import java.sql.Date

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
    "find row by id" in new tdata {
      val q = Artists.createFinderBy(_.id)
      q(1001).firstOption must beSome
      q(1001).first must_== (1001, "Tool", "", Genre.Rock, date("1990-01-02"), None)

      q.first(1001) must_== (1001, "Tool", "", Genre.Rock, date("1990-01-02"), None)
      q.firstOption(1001) must beSome

      q.firstOption(999) must beNone
      q.first(999) must throwA[NoSuchElementException]
    }
    "map duration" in new tdata {
      val q = for (s <- Songs if s.id === 1001) yield s.x
      q.first must_== (1001, "Vicarious", Duration(7,6))

      val q2 = Songs.createFinderBy(_.id)
      q2.first(1001) must_== (1001, "Vicarious", Duration(7,6), 1, 1004)
    }
    "sum duration" in new tdata {
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

      val q2 = Albums.filter(_.rating === Six.asInstanceOf[Rating])
//      val q2 = for (a <- Albums if a.rating === Six.asInstanceOf[Rating]) yield a
      val l2 = q2.list()

      l2.size must_== 2

//      val q3 = Albums.filter(_.rating === None.asInstanceOf[Option[Rating]])
      val q3 = Albums.filter(_.rating === null.asInstanceOf[Option[Rating]])
//      q3.selectStatement must_== ""
      val l3 = q3.list
      l3.size must_== 25

    }
    "query for persons with None" in new tdata {
//      val q = Persons.filter(_.biography === None.asInstanceOf[Option[String]])
      val q = Persons.filter(_.biography === null.asInstanceOf[Option[String]])
//      q.selectStatement must_== ""
      val l = q.list
      l.size must_== 22
    }
  }

  implicit def date(dateStr : String) : Date = {
    Date.valueOf(dateStr)
  }
}
