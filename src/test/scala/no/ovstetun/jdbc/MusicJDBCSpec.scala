package no.ovstetun.jdbc

import org.specs2.mutable.{After, Specification}
import no.ovstetun.{Genre, DBSupport}
import java.sql._

class MusicJDBCSpec extends Specification with DBSupport {

  def using[Closeable <: {def close()}, X](r:Closeable)(f:Closeable => X) = {
    try {
      f(r)
    } finally {
      r.close()
    }
  }

  trait t extends After {
    implicit val con = ds.getConnection
    con.setAutoCommit(false)

    def stmt = con.createStatement()

    def after {
      if (!con.isClosed)
        con.rollback()
    }
  }

  "jdbc" should {
    "run a count" in new t {
      loadData

      val q = "SELECT count(*) from ARTISTS"
      val rs = stmt.executeQuery(q)
      rs.next() must beTrue
      rs.getInt(1) must_== 5
    }
    "perform an insert" in new t {
      loadData
      
      val in = "INSERT INTO ARTISTS(name, biography, founded, maingenre) VALUES (?,?,?,?)"
      val ps = con.prepareStatement(in, Statement.RETURN_GENERATED_KEYS)
      ps.setString(1, "Tool")
      ps.setString(2, "bla bla bla...")
      ps.setDate(3, Date.valueOf("1990-01-02"))
      ps.setInt(4, Genre.Rock.id)

      ps.executeUpdate() must_== 1

      val rs = ps.getGeneratedKeys
      rs.next()
      rs.getInt(1) must beGreaterThan(0)
    }
    "create album for artist" in new t {
      loadData

      val in = "INSERT INTO ALBUMS(name, release, rating, artist_id) VALUES (?,?,?,?)"
      val ps = con.prepareStatement(in)
      ps.setString(1, "New one")
      ps.setDate(2, Date.valueOf("2011-09-07"))
      ps.setInt(3, 3)
      ps.setInt(4, 1001)

      ps.executeUpdate() must_== 1
    }
    "update an artist" in new t {
      loadData

      val ps = con.prepareStatement("UPDATE ARTISTS SET name = ? WHERE id = ?")
      ps.setString(1, "updated...")
      ps.setInt(2, 1001)
      ps.executeUpdate() must_== 1
    }
    "update all tool albums" in new t {
      loadData

      val ps = con.prepareStatement("UPDATE ALBUMS SET rating = ? WHERE artist_id = ?")
      ps.setInt(1, 5)
      ps.setInt(2, 1001)
      ps.executeUpdate() must_== 4
    }
    "fetch artist by id, wrapping statements correctly" in new t {
      loadData

      var conn : Connection = null
      var st : PreparedStatement = null

      val a = try {
        conn = con
        st = conn.prepareStatement("SELECT id, name, biography, founded, split, maingenre FROM ARTISTS WHERE id=?")
        st.setInt(1, 1001)
        val rs = st.executeQuery()
        if (rs.next()) {
          val id = rs.getInt(1)
          val name = rs.getString(2)
          val bio = rs.getString(3)
          val founded = rs.getDate(4)
          val split = rs.getDate(5)
          val splitO = if (split != null) Some(split) else None
          val gen = Genre(rs.getInt(6))

          id must_== 1001
          name must_== "Tool"
          gen must_== Genre.Rock

          Some(new Artist(name, bio, founded, splitO, gen))
        } else {
          None
        }
      } finally {
        st.close()
        conn.close()
      }
      
      a must beSome
    }
    "find all artists" in new t {
      loadData

      var res = List[Artist]()

      val st = con.prepareStatement("SELECT id, name, biography, founded, split, maingenre FROM ARTISTS")
      val rs = st.executeQuery()
      while (rs.next()) {
        val name = rs.getString(2)
        val bio = rs.getString(3)
        val founded = rs.getDate(4)
        val split = rs.getDate(5)
        val splitO = if (split != null) Some(split) else None
        val gen = Genre(rs.getInt(6))

        val a = new Artist(name, bio, founded, splitO, gen)
        res ::= a
      }
      res = res.reverse

      res.size must_== 5
    }
    "fetch artist by id, wrapping with using" in new t {
      loadData

      val a = using(con){c =>
        using(c.prepareStatement("SELECT id, name, biography, founded, maingenre FROM ARTISTS WHERE id=?")){ps =>
          ps.setInt(1, 1001)
          using(ps.executeQuery()) {rs =>
            if (rs.next) {
              val id = rs.getInt(1)
              val name = rs.getString(2)
              val bio = rs.getString(3)
              val founded = rs.getDate(4)
              val gen = Genre(rs.getInt(5))

              id must_== 1001
              name must_== "Tool"
              gen must_== Genre.Rock

              Some(new Artist(name, bio, founded, None, gen))
            } else {
              None
            }
          }
        }
      }
      a must beSome
    }
    "fetch album with songs" in new t {
      loadData
      val q =
        """
        SELECT a.id, a.name, a.release, a.rating, s.name, s.duration, s.tracknumber
        FROM Albums a JOIN Songs s ON s.album_id = a.id
        WHERE a.id = ?
        ORDER BY s.tracknumber ASC
        """
      val ps = con.prepareStatement(q)
      ps.setInt(1, 1004)
      val rs = ps.executeQuery()
      rs.next must_== true
//      while (rs.next) {
//        // extract song, add to list
//      }
      // extract album from last line, add songs to artist
    }
    "order by duration of album" in new t {
      loadData

      val q =
        """
        SELECT a.id, a.name, sum(s.duration)
        FROM Albums a, Songs s
        WHERE s.album_id = a.id
        GROUP BY a.id
        ORDER BY sum(s.duration)
        """

      var result = List[(Int, String, Long)]()
      var expected = (1003, "Lateralus", 4731) :: (1004, "10000 Days", 4545) :: Nil

      val pstmt = con.prepareStatement(q)
      val rs = pstmt.executeQuery()
      while (rs.next()) {
        result = (rs.getInt(1), rs.getString(2), rs.getLong(3)) :: result
      }
      result.reverse
      result must_== expected
    }
  }
}
