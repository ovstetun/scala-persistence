package no.ovstetun.jdbc

import org.specs2.mutable.Specification
import no.ovstetun.DBSupport
import org.specs2.execute.Result
import java.sql.Statement
import org.specs2.specification.{AroundExample, Around}


class JDBCSpec extends Specification with AroundExample with DBSupport {
  implicit lazy val con = ds.getConnection
  def stmt = con.createStatement()

  def around[T <% Result](t: => T) = {
    con.setAutoCommit(false)
    val res = t
    con.rollback()
    res
  }


  "jdbc" should {
    "run a count" in {
      loadData

      val q = "SELECT count(*) from USERS"
      val res = stmt.executeQuery(q)
      res.next() must beTrue
      res.getInt(1) must_== 4
    }
    "perform an insert" in {
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
