package no.ovstetun

object Genre extends Enumeration {
  type Genre = Value

  val Rock = Value(1)
  val Pop = Value(2)
  val Classic = Value(3)
  val Blues = Value(4)
  val Rap = Value(5)
  val HipHop = Value(6)
  val Alternative = Value(7)

}
