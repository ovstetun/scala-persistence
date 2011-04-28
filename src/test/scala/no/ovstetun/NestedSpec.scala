package no.ovstetun

import org.specs2.mutable._

class NestedSpec extends Specification {
  "A Spec" should {
    val s = "abc"
    "jump past first level" in {
      "execute second" in {
        s must have size(3)
      }
      "but jump past next level" in {
        val d = s.length
        "until the innermost" in {
          d must_== 3
        }
        "and the second innermost" in {
          true must_== true
        }
      }
    }
  }
}
