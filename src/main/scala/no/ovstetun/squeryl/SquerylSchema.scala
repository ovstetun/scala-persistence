package no.ovstetun.squeryl

import org.squeryl.{Schema, KeyedEntity}

case class User(var firstname:String, var lastname:String) extends KeyedEntity[Int] {
  val id:Int = 0
}
object SquerylSchema extends Schema {
  val users = table[User]("users")

  def save(user:User) = {
    users.insert(user)
  }
}
