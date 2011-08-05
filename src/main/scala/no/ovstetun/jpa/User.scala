package no.ovstetun.jpa

import javax.persistence.{GenerationType, GeneratedValue, Id, Entity}

@Entity
class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id : Long = _
  var firstname : String = _
  var lastname : String = _
}
