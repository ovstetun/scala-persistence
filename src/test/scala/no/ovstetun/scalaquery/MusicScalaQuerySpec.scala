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
      q(1001).first must_== (1001, "Tool", "", date("1990-01-02"), None)

      q.first(1001) must_== (1001, "Tool", "", date("1990-01-02"), None)
      q.firstOption(1001) must beSome

      q.firstOption(999) must beNone
      q.first(999) must throwA[NoSuchElementException]
    }
  }

  implicit def date(dateStr : String) : Date = {
    Date.valueOf(dateStr)
  }
}
