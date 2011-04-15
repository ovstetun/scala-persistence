package no.ovstetun

import org.specs2.mutable._

class NestedSpec extends Specification {
  "a" should {
    val s = "abc"
    "b" in {
      "c" in {
        s must have size(3)
      }
      "d" in {
        val d = s.length
        "e" in {
          d must_== 3
        }
        "success" in {
          true must_== true
        }
      }
    }
  }
}
