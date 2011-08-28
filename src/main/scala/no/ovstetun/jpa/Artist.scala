package no.ovstetun
package jpa

import javax.persistence.GeneratedValue._
import javax.persistence._
import java.util.Date
import org.eclipse.persistence.annotations.{Convert, Converter}

@Entity
@Table(name = "artists")
class Artist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id : Int = _
  var name : String = _
  var biography : String = _
  @Temporal(value = TemporalType.DATE)
  var founded : Date = _

//  @Enumerated
  @Converter(name = "GenreConverter", converterClass = classOf[GenreConverter])
  @Convert("GenreConverter")
  var maingenre : Genre.Value = Genre.Rock
}

@Entity
@Table(name = "albums")
class Album {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id : Int = _
  var name : Int = _
  @Temporal(value = TemporalType.DATE)
  var release : Date = _
  var rating : Int = _
}
