package no.ovstetun.sdummy

import java.util.Date

class Person(var firstname:String, var lastname:String, var birthdate:Date = null)

class Person2(var firstname:String, var lastname:String, var birthdate:Option[Date] = None)

sealed trait Gender
case object Male extends Gender
case object Female extends Gender

object GenderE extends Enumeration {
  val Male, Female = Value
}
