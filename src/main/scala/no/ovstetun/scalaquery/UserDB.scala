package no.ovstetun.scalaquery

import org.scalaquery.ql._
import extended.{ExtendedProfile, ExtendedTable => Table}

trait UserDB {
  self : ExtendedProfile =>
  import self.Implicit._

  object Users extends Table[(Int, String, String, Option[String])]("USERS") {
    def id = column[Int]("ID", O PrimaryKey, O AutoInc)
    def firstname = column[String]("FIRSTNAME")
    def lastname = column[String]("LASTNAME")
    def email = column[Option[String]]("EMAIL")

    def a = id ~ firstname ~ lastname <> (User, User.unapply _)

    def * = id ~ firstname ~ lastname ~ email
    def forinsert = firstname ~ lastname
  }

  object Posts extends Table[(Int, String, Option[String], Int)]("POSTS") {
    def id = column[Int]("ID", O PrimaryKey, O AutoInc)
    def title = column[String]("TITLE")
    def content = column[Option[String]]("CONTENT")
    def author_id = column[Int]("AUTHOR")

    def * = id ~ title ~ content ~ author_id
    def forinsert = title ~ content ~ author_id
  }
}

case class User(id:Int, firstname:String, lastname:String)
case class Post(id:Int, title:String, content:Option[String] = None)
