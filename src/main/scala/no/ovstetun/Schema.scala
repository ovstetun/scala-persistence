package no.ovstetun

import org.scalaquery.ql._
import extended.{ExtendedProfile, ExtendedTable => Table}

trait Schema {
  self : ExtendedProfile =>
  import self.Implicit._

  object Users extends Table[(Int, String, String)]("USERS") {
    def id = column[Int]("id", O PrimaryKey, O AutoInc)
    def firstname = column[String]("firstname")
    def lastname = column[String]("lastname")

    def * = id ~ firstname ~ lastname
    def forinsert = firstname ~ lastname
  }

  case class A(id : Option[Int], name : String)
  object AT extends Table[A]("a") {
    def id = column[Int]("id", O PrimaryKey, O AutoInc)
    def name = column[String]("name")

    def * = id.? ~ name <> (A, A.unapply _)
  }

  def ddl = Users.ddl
  
}