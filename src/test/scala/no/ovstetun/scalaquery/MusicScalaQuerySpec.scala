package no.ovstetun
package scalaquery

import no.ovstetun.DBSupport
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.extended.H2Driver
import org.specs2.execute.Result
import org.specs2.mutable.{Around, Specification}
import java.sql.Date
import org.scalaquery.ql.{Parameters, Join, Query}
import org.scalaquery.ql.Ordering.Desc
import org.scalaquery.simple.{GetResult, StaticQuery}
import org.scalaquery.session.{PositionedResult, Database}

class MusicScalaQuerySpec extends Specification with DBSupport {
//  def myDs : DataSource = {...}
  lazy val db = Database.forDataSource(ds)
  lazy val db2 = Database.forName("jdbc/my_datasource") // JNDI lookup
  lazy val db3 = Database.forURL("jdbc:h2:test", user = "sa", password = "")

  implicit def conn = threadLocalSession.conn

  val s = new H2Driver with MusicDB
  import s.Implicit._
  import s._

  trait tdata extends Around {
    def around[T <% Result](t: => T) = db withSession {
      threadLocalSession withTransaction {
        loadData

        val res = t
        threadLocalSession.rollback()
        res
      }
    }
  }

  "scalaquery" should {
    "StaticQuery" in {
      "count using sql" in new tdata {
        val q = StaticQuery[Int] + "select count(id) from Artists"
        q.first must_== 5

        val q2 = StaticQuery.queryNA[Int]("select count(id) from Artists")
        q2.first must_== 5
      }
      "select using sql" in new tdata {
        val q = StaticQuery[Int, (Int, String)] + "select id, name from Artists where id = ?"
        q(1001).first must_== (1001, "Tool")
        val (id,name) = q(1001).first
        val artist : (Int, String) = q(1001).first

        q(1001).list() must_== List((1001, "Tool"))

        val q2 = StaticQuery.query[Int, (Int, String)]("select id, name from Artists where id = ?")
        q2(1001).first must_== (1001, "Tool")
      }
      "insert using sql" in new tdata {
        import org.scalaquery.simple.StaticQuery._
        val q = query[(String, String), Int]("insert into Artists(name, biography) values (?, ?)")
        val q2 = q("Seigmen", "")
        q2.execute()
        q2.first must_== 1
      }
      "select to case class from sql" in {
        case class Art(id:Int, name:String)
        "with explicit mapping" in new tdata {
          val q = StaticQuery.query[Int, (Int, String)]("select id, name from Artists where id = ?")
          val qMapped = q.mapResult[Art]({case (a, b) => Art(a, b)})
          qMapped(1001).first() must_== Art(1001, "Tool")
        }
        "with implicit mapping" in new tdata {
          implicit val artMapper = GetResult((r: PositionedResult) => new Art(r<<, r<<))

          val q = StaticQuery.query[Int, Art]("select id, name from Artists where id = ?")
          q(1001).first must_== Art(1001, "Tool")
        }
      }
      "using database functions " in new tdata {
        import org.scalaquery.simple.{StaticQuery => Q}
        import Q._

        val q = queryNA[Int]("select 1 + 1")
        q.first must_== 2

        val qRand = Q[String] + "select random_uuid()"
        qRand.first must_!= ""
      }
    }
    "count artists" in new tdata {
      val i : Int = Query(Artists.count).first()
      i must_== 5

      val q = for (a <- Artists) yield a.id.count
      q.first must_== 5
    }
    "find artist row by id" in new tdata {
      val q = Artists.createFinderBy(_.id)
      q(1001).firstOption must beSome
      q(1001).first must_== (1001, "Tool", "", Genre.Rock, date("1990-01-02"), None)

      q.first(1001) must_== (1001, "Tool", "", Genre.Rock, date("1990-01-02"), None)
      q.firstOption(1001) must beSome

      q.firstOption(999) must beNone
      q.first(999) must throwA[NoSuchElementException]


      def findArtist(id:Int) = {
        val q = Artists.createFinderBy(_.id)
        q(id).firstOption
      }
      findArtist(1001) must beSome
    }
    "insert single artist" in new tdata {
      val i = Artists.i.insert(("Seigmen", "", Genre.Rock, date("1989-12-27"), Some(date("2008-06-22"))))
      i must_== 1
    }
    "insert a batch of artists" in new tdata {
      val artists = List(
        ("Seigmen", "", Genre.Rock, date("1989-12-27"), Some(date("2008-06-22"))),
        ("Muse", "", Genre.Rock, date("1994-06-01"), None),
        ("Oslo Ess", "", Genre.Rock, date("2010-06-01"), None)
      )
      val i = Artists.i.insertAll(artists :_*)
      i must beSome(3)
    }
    "update a row" in new tdata {
      val q = for {
        a <- Artists if a.id === 1001
      } yield a.name

      q.first must_== "Tool"
      q.update("updated") must_== 1
    }
    "update albums" in new tdata {
      val q = for (a <- Albums if a.artist_id === 1001) yield a.rating
      q.update(Some(Six)) must_== 4
    }
    "delete an album" in new tdata {
      val q = for (a <- Albums if a.id === 1001) yield a
      q.delete must_== 1
    }
    "map duration column to Duration case class" in new tdata {
      val q = for (s <- Songs if s.id === 1001) yield s.x
      q.first must_== (1001, "Vicarious", Duration(7,6))

      val q2 = Songs.createFinderBy(_.id)
      q2.first(1001) must_== (1001, "Vicarious", Duration(7,6), 1, 1004)
    }
    "find total length of album from a query with mapping to Duration" in new tdata {
      val q = for (s <- Songs if s.album_id === 1004) yield s.duration.sum
      q.first must_== Some(Duration(75, 45))
    }
    "map rating for album" in new tdata {
      val q = Albums.createFinderBy(_.id)
      q.first(1004) must_== (1004, "10000 Days", date("2006-05-02"), None, 1001)
      q.first(1003) must_== (1003, "Lateralus", date("2001-05-15"), Some(Six), 1001)
    }
    "insert an album with case object as rating" in new tdata {
      Albums.i.insert("lala", date("2006-05-02"), Some(Six), 1001) must_== 1
      Albums.i.insert("lala2", date("2006-05-02"), None, 1001) must_== 1
      Albums.i.insert("lala3", date("2006-05-02"), None, 1001) must_== 1

      val q = Query(Albums.count)
      q.first must_== 27

      val q2 = Albums.filter(_.rating === (Six:Rating))
      val l2 = q2.list()

      l2.size must_== 2

      val q3 = Albums.filter(_.rating isNull) //=== (null:Rating))
      val l3 = q3.list
      l3.size must_== 25

    }
    "query for persons with None" in new tdata {
      val q = Persons.filter(_.biography isNull) //=== (null:Option[String]))
      val l = q.list
      l.size must_== 22
    }
    "Find rockers" in new tdata {
      val q1 = Artists.filter(_.maingenre === Genre.Rock)
      val q = for {
        a <- Artists if a.maingenre === Genre.Rock
      } yield a.id ~ a.name

      q.list must_== List((1001, "Tool"), (1005, "A Perfect Circle"))
    }
    "find rockers yield as tuple" in new tdata {
      val q = for {
        a <- Artists if a.maingenre === Genre.Rock
      } yield (a.id, a.name)

      q.list must_== List((1001, "Tool"), (1005, "A Perfect Circle"))
    }
    "find rockers with albumcount" in new tdata {
      val q = for {
        Join(a, al) <- Artists leftJoin Albums on (_.id === _.artist_id)
        c <- Query(al.id.count)
        _ <- Query groupBy(a.id)
        _ <- Query orderBy(Desc(c))
        if a.maingenre === Genre.Rock
      } yield a.id ~ a.name ~ c

      val l = q.list
      l must_== List((1001, "Tool", 4), (1005, "A Perfect Circle", 0))

      val q2 = q.take(2).drop(1)
      q2.list must_== List((1005, "A Perfect Circle", 0))

      val q3 = q.take(1)
      q3.list must_== List((1001, "Tool", 4))
    }
    "find artists with albums" in new tdata {
      val q = for {
        a <- Artists
        al <- Albums
        if a.id === al.artist_id
      } yield a.id ~ a.name
      q.list.toSet must_== Set((1001, "Tool"), (1002, "Pink Floyd"), (1003, "Arcade Fire"))

      case class ArtistInfo(id:Int, name:String)
      val qM = q.mapResult({case (id, name) => ArtistInfo(id, name)})
      val l = qM.list.toSet
      l.size must_== 3

      val q2 = for {
        a <- Artists
        if a.id in (for (al <- Albums) yield al.artist_id)
      } yield a.id ~ a.name
      q2.list must_== List((1001, "Tool"), (1002, "Pink Floyd"), (1003, "Arcade Fire"))
    }
    "find artist as a function by name as parameter" in new tdata {
      def byName(n:String) = for (a <- Artists if a.name === n.bind) yield a.id ~ a.name
      byName("Tool").first must_== (1001, "Tool")
      byName("Pink Floyd").first must_== (1002, "Pink Floyd")
      byName("don't think so").first must throwA[NoSuchElementException]

      byName("Tool").firstOption must beSome((1001, "Tool"))
      byName("nope").firstOption must beNone

//      var name = ""
//      val byName2 = for (a <- Artists if a.name === name.bind) yield a.id ~ a.name
//      byName2.firstOption must beNone
//
//      name = "Tool"
//      byName2.firstOption must beNone
      //      byName2.firstOption must beSome((1001, "Tool"))
    }
    "find artist as a value by name as parameter" in new tdata {
      val qArtist = for {
        n <- Parameters[String]
        a <- Artists if a.name === n
      } yield a.id ~ a.name

//      val qArtist2 = for {
//        (n, x) <- Parameters[(Long, Int)]
//        a <- Artists if a.id === n
//      } yield a.id ~ a.name
//      qArtist2("Tool").first() must_== (1001, "Tool")

      val qA = qArtist("Tool")

      qArtist("Tool").first must_== (1001, "Tool")
      qArtist("lala").firstOption must beNone
    }
    "find all artists with more than 4 albums" in new tdata {
      val q = for {
        al <- Albums
        a  <- al.artist
        c  <- Query(al.id.count)
        _  <- Query groupBy(a.id)
        _  <- Query having(_ => c > 4)
      } yield a.id ~ a.name

      q.list.size must_== 1
      q.first must_== (1002, "Pink Floyd")
    }
    "join albums with artists" in new tdata {
      val q = for {
        al <- Albums
        a  <- al.artist
      } yield a.name ~ al.name

      q.list.size must_== 24
      q.first() must_== ("Tool", "Undertow")

      val q2 = for {
        al <- Albums
        a  <- Artists
        if al.artist_id === a.id
      } yield a.name ~ al.name
      q2.list.size must_== 24

      val x2 = for {
        al <- Albums
        a <- Artists
        if al.artist_id === a.id
      } yield a.id.countDistinct
      x2.first must_== 3

//      val q3 = for {
//        (al, a) <- Albums innerJoin Artists on (_.artist_id is _.id)
//      } yield a.name ~ al.name
//      q3.list.sixe must_== 24

      val x = for {
        a <- q2
      } yield a._1.count
      x.first must_== 24
    }
    "find artists without albums" in new tdata {
      val q = for {
        a <- Artists
        if a.id notIn (for (al <- Albums) yield al.artist_id)
      } yield a.id ~ a.name
      q.list.size must_== 2
      q.list must_== List((1004,"Jay-Z"), (1005, "A Perfect Circle"))
    }
    "find all persons with more than one artist" in new tdata {
      val q = for {
        pa <- PersonsArtists
        a  <- pa.artist
        p  <- pa.person
        n  <- Query(a.id.count)
        _  <- Query groupBy(p.id)
        _  <- Query having(_ => n > 1)
      } yield p.id ~ p.firstname ~ p.lastname

      q.list must_== List((1001, "Maynard James", "Keenan"))
    }
    "find length of albums" in new tdata {
      val byId = for {
        id <- Parameters[Int]
        Join(al, s) <- Albums leftJoin Songs on(_.id === _.album_id)
        len <- Query(s.duration.sum)
        _ <- Query groupBy(al.id)
//        _ <- Query orderBy(Desc(len))
        if (al.id === id)
      } yield al.id ~ len

      byId.first(1003) must_== (1003, Some(Duration(78,51)))
    }
    "expand queries" in new tdata {
      val q1 = for {
        a <- Artists
      } yield a

      val q2 = for (a <- q1) yield a.id ~ a.name
      q2.selectStatement must_!= q1.selectStatement
    }
  }

  implicit def date(dateStr : String) : Date = {
    Date.valueOf(dateStr)
  }
}
