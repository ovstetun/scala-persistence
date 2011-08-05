package no.ovstetun.scalaquery

import org.scalaquery.ql._
import extended.{ExtendedProfile, ExtendedTable => Table}

trait Schema {
  self : ExtendedProfile =>
  import self.Implicit._

  object Users extends Table[(Int, String, String)]("USERS") {
    def id = column[Int]("id", O PrimaryKey, O AutoInc)
    def firstname = column[String]("FIRSTNAME")
    def lastname = column[String]("LASTNAME")

    def * = id ~ firstname ~ lastname
    def forinsert = firstname ~ lastname
  }

}
