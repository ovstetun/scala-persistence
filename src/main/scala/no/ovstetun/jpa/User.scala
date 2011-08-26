package no.ovstetun.jpa

import javax.persistence._

@Entity
@Table(name = "users")
class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id : Long = _
  var firstname : String = _
  var lastname : String = _
  var email : String = _
}
