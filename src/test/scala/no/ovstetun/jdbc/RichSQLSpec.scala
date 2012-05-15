package no.ovstetun.jdbc

import org.specs2.mutable.{After, Specification}
import no.ovstetun.Genre._
import java.sql.Date
import no.ovstetun.{Genre, DBSupport}

class RichSQLSpec extends Specification with DBSupport {
  trait t extends After {
    implicit val con = ds.getConnection
    implicit def stmt = con.createStatement

    con.setAutoCommit(false)
    loadData

    def after {
      if (!con.isClosed)
        con.rollback()
    }
  }

  import RichSQL._

  "RichSQL" should {
    "fetch an artist" in new t {
      val ps = con.prepareStatement("SELECT name, biography, founded, maingenre FROM ARTISTS WHERE id=?")
      val a = ps << 1001 <<! {rs =>
//      val a = "SELECT name, biography, founded, maingenre FROM ARTISTS WHERE id=?" << 1001 <<! {rs =>
        new Artist(rs, rs, rs, None, Genre(rs))
      }
      val artist = if (a.isEmpty) None else Some(a.head)
      artist must beSome

      a.size must_== 1
      a.head.name must_== "Tool"
      a.head.mainGenre must_== Genre.Rock
    }
    "insert an artist" in new t {
      val c = query("SELECT COUNT(id) FROM ARTISTS", {rs =>
        rs.nextInt
      })

      val q : RichPreparedStatement = "INSERT INTO ARTISTS(name, biography, founded, maingenre) VALUES (?,?,?,?)"
      q << "Tool" << "bla bla..." << Date.valueOf("1990-01-02") << Rock.id <<!

      val c2 = query("SELECT COUNT(id) FROM ARTISTS", {rs =>
        rs.nextInt
      })
      (c.head + 1) must_== c2.head
    }
  }
}
