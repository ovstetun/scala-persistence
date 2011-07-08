package no.ovstetun

import org.specs2.mutable._
import org.specs2.execute._
import org.specs2.specification._

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql._
import extended.{ExtendedProfile, ExtendedTable => Table}
import org.scalaquery.ql.extended.H2Driver
import java.sql.SQLException

trait DBSpec extends Specification with AroundExample {
  args(sequential=true)

  lazy val db : Database = Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")
  val driver = H2Driver
  import driver.Implicit._


  def around[T <% Result](t: => T) = db withSession {
    MyTable.ddl.create

    t
  }

  object withData extends Before {
    def before = {
      MyTable.insert (1, "test")
      MyTable.insert (2, "test2")
    }
  }
}

class MyTableSpec extends DBSpec {
  import driver.Implicit._

  "MyTable" should {
    "allow insert" in {
      (MyTable.insert(14, "lala") must_== 1)
    }
    "fail to insert a duplicate key" in {
      MyTable.insert(1, "1") must_== 1
      MyTable.insert(1, "1") must throwA[SQLException]
    }
    "contain two rows from basic data" in withData {
      val q = Query(MyTable.count)
      q.first must_== 2
    }
    "contain three rows after insert" in withData {
      MyTable.insert(3, "test3")
      Query(MyTable.count).first must_== 3
    }
  }
}


object MyTable extends Table[(Int, String)] ("mytable") {
  def id = column[Int]("id", O PrimaryKey)
  def name = column[String]("name")

  def * = id ~ name
}
