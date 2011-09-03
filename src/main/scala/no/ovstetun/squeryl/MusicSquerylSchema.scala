package no.ovstetun
package squeryl

import java.util.Date
import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.CompositeKey2

object MusicSquerylSchema extends Schema {
  val artists = table[Artist]("ARTISTS")
  val albums = table[Album]("ALBUMS")
  val songs = table[Song]("SONGS")
  val persons = table[Person]("PERSONS")

  val artistsToAlbums = oneToManyRelation(artists, albums).via((a, alb) => (alb.artist_id === a.id))
  val albumsToSongs = oneToManyRelation(albums, songs).via((a, s) => (s.album_id === a.id))
  val personsToArtists = manyToManyRelation(persons, artists).
          via[PersonArtist]((p, a, pa) => (pa.person_id === p.id, pa.artist_id === a.id))
}

import MusicSquerylSchema._

class Artist(val name:String, val biography:String, val maingenre:Genre.Genre, val founded:Date, val split:Option[Date]) extends KeyedEntity[Int] {
  val id :Int = 0
  def this() = this("", "", Genre.Rock, new Date(), Some(new Date()))

  lazy val albums = artistsToAlbums.left(this)
  lazy val persons = personsToArtists.right(this)
}

class Album(val name:String, val release:Date, val rating:Option[Int], val artist_id:Int) extends KeyedEntity[Int] {
  val id :Int = 0
  def this() = this("", new Date(), Some(0), 0)

  lazy val artist = artistsToAlbums.right(this)
  lazy val songs = albumsToSongs.left(this)
}

class Song(val name:String, val duration:Int, val tracknumber:Int, val album_id:Int) extends KeyedEntity[Int] {
  val id:Int = 0
}

class Person(val firstname:String, val lastname:String, val biography:String) extends KeyedEntity[Int] {
  val id:Int = 0
}

class PersonArtist(val person_id:Int, val artist_id:Int) extends KeyedEntity[CompositeKey2[Int,Int]] {
  def id = CompositeKey2(person_id, artist_id)
}
