package no.ovstetun
package scalaquery

import org.scalaquery.ql._
import extended.{ExtendedProfile, ExtendedTable => Table}
import java.sql.Date


case class Duration(mins:Int, secs:Int)

sealed abstract class Rating(val value:Int)
object Rating {
  def apply(value:Int) = value match {
    case 6 => Six
    case 5 => Five
    case 4 => Four
    case 3 => Three
    case 2 => Two
    case 1 => One
    case _ => null.asInstanceOf[Rating]
  }
}

case object Six extends Rating(6)
case object Five extends Rating(5)
case object Four extends Rating(4)
case object Three extends Rating(3)
case object Two extends Rating(2)
case object One extends Rating(1)
//case object NoRating extends Rating(null.asInstanceOf[Int])

trait MusicDB {
  self : ExtendedProfile =>
  import self.Implicit._

//  implicit object GenreMapper extends MappedTypeMapper[Genre.Genre, Int] with BaseTypeMapper[Genre.Genre] {
//    def map(t: Genre.Genre) = t.id
//    def comap(u: Int) = Genre(u)
//  }
  implicit val genreMapper = MappedTypeMapper.base[Genre.Genre, Int](_.id, Genre(_))
//  implicit val durationMapper = MappedTypeMapper.base[Duration, Int](
//    dur => dur.mins * 60 + dur.secs,
//    secs => Duration(secs / 60, secs % 60)
//  )
  implicit object durationMapper extends MappedTypeMapper[Duration, Int] with BaseTypeMapper[Duration] with NumericTypeMapper {
    def map(dur: Duration) = dur.mins * 60 + dur.secs
    def comap(secs: Int) = Duration(secs / 60, secs % 60)
  }
  implicit val ratingMapper = MappedTypeMapper.base[Rating, Int](_.value, Rating(_))

  object Artists extends Table[(Int, String, String, Genre.Genre, Date, Option[Date])]("ARTISTS") {
    def id = column[Int]("ID", O PrimaryKey, O AutoInc)
    def name = column[String]("NAME")
    def biography = column[String]("BIOGRAPHY")
    def maingenre = column[Genre.Genre]("MAINGENRE")
    def founded = column[Date]("FOUNDED")
    def split = column[Option[Date]]("SPLIT")

    def * = id ~ name ~ biography ~ maingenre ~ founded ~ split
    def i = name ~ biography ~ maingenre ~ founded ~ split
  }
  object Albums extends Table[(Int, String, Date, Option[Rating], Int)]("ALBUMS") {
    def id = column[Int]("ID", O PrimaryKey, O AutoInc)
    def name = column[String]("NAME", O NotNull)
    def release = column[Date]("RELEASE")
    def rating = column[Option[Rating]]("RATING")
    def artist_id = column[Int]("ARTIST_ID")

    def artist = foreignKey("albums_artists_fk", artist_id, Artists)(_.id)

    def i = name ~ release ~ rating ~ artist_id
    def * = id ~ name ~ release ~ rating ~ artist_id
  }
  object Songs extends Table[(Int, String, Duration, Int, Int)]("SONGS") {
    def id = column[Int]("ID", O PrimaryKey, O AutoInc)
    def name = column[String]("NAME", O NotNull)
    def duration = column[Duration]("DURATION")
    def tracknumber = column[Int]("TRACKNUMBER")
    def album_id = column[Int]("ALBUM_ID")

    def album = foreignKey("songs_albums_fk", album_id, Albums)(_.id)

    def x = id ~ name ~ duration
    def * = id ~ name ~ duration ~ tracknumber ~ album_id
  }
  object Persons extends Table[(Int, String, String, Option[String])]("PERSONS") {
    def id = column[Int]("ID", O PrimaryKey, O AutoInc)
    def firstname = column[String]("FIRSTNAME", O NotNull)
    def lastname = column[String]("LASTNAME", O NotNull)
    def biography = column[Option[String]]("BIOGRAPHY", O NotNull)

    def * = id ~ firstname ~ lastname ~ biography
  }
  object PersonsArtists extends Table[(Int, Int)]("PERSON_ARTIST") {
    def artist_id = column[Int]("ARTIST_ID", O NotNull)
    def person_id = column[Int]("PERSON_ID", O NotNull)

    def pk = primaryKey("personartists_pk", artist_id ~ person_id)
    def artist = foreignKey("person_artist_a_fk", artist_id, Artists)(_.id)
    def person = foreignKey("person_artist_p_fk", person_id, Persons)(_.id)

    def * = artist_id ~ person_id
  }

}
