package no.ovstetun
package jdbc

import java.sql.Date
import Genre._

class Artist(val name: String, val biography: String,
             val founded: Date, val split: Option[Date],
             val mainGenre: Genre, val albums: List[Album] = Nil, val persons: List[Person] = Nil)

class Album(val name: String, val release: Date, val rating: Int,
            val songs: List[Song] = Nil)

class Song(val name: String, val duration: Int, val tracknumber: Int)

class Person(val firstname: String, val lastname: String,
             val biography: String)
