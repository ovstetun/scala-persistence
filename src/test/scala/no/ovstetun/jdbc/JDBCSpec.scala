package no.ovstetun.jdbc

import org.specs2.mutable.Specification
import no.ovstetun.DBSupport
import org.specs2.specification.Around
import org.specs2.execute.Result
import java.sql.Statement


class JDBCSpec extends Specification with DBSupport {
  lazy val con = ds.getConnection
  def stmt = con.createStatement()

  object a extends Around {
    def around[T <% Result](t: => T) = {
      con.setAutoCommit(false)
      val res = t
      con.rollback()
      res
    }
  }

  "jdbc" should {
    "run a count" in a {
      val q = "SELECT count(*) from USERS"
      val res = stmt.executeQuery(q)
      res.next() must beTrue
      res.getInt(1) must_== 0
    }
    "perform an insert" in a {
      val in = "INSERT INTO users(firstname, lastname) VALUES (?,?)"
      val pstmt = con.prepareStatement(in, Statement.RETURN_GENERATED_KEYS)
      pstmt.setString(1, "bob")
      pstmt.setString(2, "bobson")

      pstmt.executeUpdate() must_== 1

      val rs = pstmt.getGeneratedKeys
      rs.next()
      rs.getInt(1) must beGreaterThan(1)
    }
  }
}
