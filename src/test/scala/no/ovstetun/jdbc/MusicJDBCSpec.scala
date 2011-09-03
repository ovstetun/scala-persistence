package no.ovstetun.jdbc

import org.specs2.mutable.{After, Specification}
import no.ovstetun.{Genre, DBSupport}
import java.sql.{ResultSet, PreparedStatement, Connection, Statement}

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
      val res = stmt.executeQuery(q)
      res.next() must beTrue
      res.getInt(1) must_== 5
    }
    "perform an insert" in new t {
      loadData
      
      val in = "INSERT INTO ARTISTS(name, biography) VALUES (?,?)"
      val pstmt = con.prepareStatement(in, Statement.RETURN_GENERATED_KEYS)
      pstmt.setString(1, "Tool")
      pstmt.setString(2, "bla bla bla...")

      pstmt.executeUpdate() must_== 1

      val rs = pstmt.getGeneratedKeys
      rs.next()
      rs.getInt(1) must beGreaterThan(0)
    }
    "fetch artist by id, wrapping statements correctly" in new t {
      loadData

      var conn : Connection = null
      var st : PreparedStatement = null
      var rs : ResultSet = null

      try {
        conn = con
        st = conn.prepareStatement("SELECT id, name, maingenre FROM ARTISTS WHERE id=?")
        st.setInt(1, 1001)
        rs = st.executeQuery()
        if (rs.next()) {
          val id = rs.getInt(1)
          val name = rs.getString(2)
          val gen = Genre(rs.getInt(3))

          id must_== 1001
          name must_== "Tool"
          gen must_== Genre.Rock
        }
      } finally {
        rs.close()
        st.close()
        conn.close()
      }
      success
    }
    "fetch artist by id, wrapping with using" in new t {
      loadData

      using(con){c =>
        using(c.prepareStatement("SELECT id, name, maingenre FROM ARTISTS WHERE id=?")){ps =>
          ps.setInt(1, 1001)
          using(ps.executeQuery()) {rs =>
            if (rs.next) {
              val id = rs.getInt(1)
              val name = rs.getString(2)
              val gen = Genre(rs.getInt(3))

              id must_== 1001
              name must_== "Tool"
              gen must_== Genre.Rock
            }
          }
        }
      }
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
