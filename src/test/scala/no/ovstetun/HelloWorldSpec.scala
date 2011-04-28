package no.ovstetun

import org.specs2.mutable._

class HelloWorldSpec extends Specification {
  "The 'Hello world' string" should {
    val s = "Hello world"

    "contain 11 characters" in {
      s must have size(11)
    }
    "start with 'Hello'" in {
      s must startWith("Hello")
    }
    "end with 'world'" in {
      s must endWith("world")
    }
  }
}
