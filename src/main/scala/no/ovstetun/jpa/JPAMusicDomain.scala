package no.ovstetun
package jpa

import Genre._

import javax.persistence._
import java.util.Date
import java.util.{List => jList}
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
  var maingenre : Genre = Genre.Rock

//  @OneToMany(orphanRemoval = true, mappedBy = "artist")
//  var albums: jList[Album] = _

  @OneToMany(orphanRemoval = true)
  @JoinColumn(name = "artist_id")
  var albums : jList[Album] = _

  @ManyToMany
  @JoinTable(name = "person_artist",
    joinColumns = Array(new JoinColumn(name = "artist_id")),
    inverseJoinColumns = Array(new JoinColumn(name = "person_id")))
  var persons : jList[Person] = _
}

@Entity
@Table(name = "albums")
class Album {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id : Int = _
  var name : String = _
  @Temporal(value = TemporalType.DATE)
  var release : Date = _
  var rating : Int = _

//  @ManyToOne
//  @JoinColumn(name = "artist_id", nullable = false)
//  var artist : Artist = _
}

@Entity
@Table(name = "persons")
class Person {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id : Int = _
  var firstname : String = _
  var lastname : String = _

  @ManyToMany(mappedBy = "persons")
  var artists : jList[Artist] = _
}
