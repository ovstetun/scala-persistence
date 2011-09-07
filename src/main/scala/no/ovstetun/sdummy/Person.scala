package no.ovstetun.sdummy

import java.util.Date

class Person(var firstname:String, var lastname:String, var gender:Gender, var birthdate:Date)

class Person2(var firstname:String, var lastname:String, var gender:Gender, var birthdate:Option[Date] = None)

sealed trait Gender
case object Male extends Gender
case object Female extends Gender

object GenderE extends Enumeration {
  val Male, Female = Value
}


trait PersonRepository {
  def findById(id:Int) : Option[Person2] = {
    val p: Person2 = new Person2("", "", Male)
    val pO : Option[Person2] = Some(p)

    val name = pO.map(pers => pers.firstname)
    name.getOrElse("John Doe")
    pO
  }

  case class MyVal(s:String)
  def myMatch(n:Any) : String = {
    n match {
      case 1 => "ONE"
      case n:Int if n < 10 => "low..."
      case n:Int if n > 10 => "high!"
      case p:Person => "a person: " + p.firstname
      case MyVal(s) => s
    }
  }
}