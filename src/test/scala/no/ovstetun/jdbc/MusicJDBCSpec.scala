package no.ovstetun.jdbc

import no.ovstetun.DBSupport
import org.specs2.execute.Result
import org.specs2.specification.{AroundExample, Around}
import java.sql.{Connection, Statement}
import org.specs2.mutable.{After, Specification}


class MusicJDBCSpec extends Specification with DBSupport {

  trait t extends After {
    implicit val con = ds.getConnection
    con.setAutoCommit(false)

    def stmt = con.createStatement()

    def after = {
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
