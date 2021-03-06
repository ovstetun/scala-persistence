package no.ovstetun
package jpa

import Genre._

import javax.persistence._
import java.util.Date
import java.util.{List => jList}
import org.eclipse.persistence.annotations.{Convert, Converter}

trait WithID {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id : Int = _
}

@Entity
@Table(name = "artists")
class Artist extends WithID {

//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  var id : Int = _
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

  @OneToMany(orphanRemoval = true, cascade = Array(CascadeType.ALL))
  @JoinColumn(name = "artist_id")
  var albums : jList[Album] = _

  @ManyToMany
  @JoinTable(name = "person_artist",
    joinColumns = Array(new JoinColumn(name = "artist_id")),
    inverseJoinColumns = Array(new JoinColumn(name = "person_id")))
  var persons : jList[Person] = _
}

@Entity
@Table(name = "persons")
class Person  extends WithID {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  var id : Int = _
  var firstname : String = _
  var lastname : String = _

  @ManyToMany(mappedBy = "persons")
  var artists : jList[Artist] = _
}

@Entity
@Table(name = "albums")
class Album extends WithID {

//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  var id : Int = _
  var name : String = _
  @Temporal(value = TemporalType.DATE)
  var release : Date = _
  var rating : Int = _

//  @ManyToOne
//  @JoinColumn(name = "artist_id", nullable = false)
//  var artist : Artist = _

  @OneToMany(orphanRemoval = true)
  @JoinColumn(name = "album_id")
  @OrderBy("")
  var songs : jList[Song] = _

  def duration : Int = {
    import scala.collection.JavaConversions._
    songs.foldLeft(0)(_ + _.duration)
  }
}

@Entity
@Table(name = "songs")
class Song extends WithID {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  var id : Int = _
  var name : String = _
  var duration : Int = _
  var tracknumber : Int = _
}
