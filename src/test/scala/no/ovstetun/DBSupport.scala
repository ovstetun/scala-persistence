package no.ovstetun

import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import org.dbunit.database.{DatabaseConnection, IDatabaseConnection}
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.util.fileloader.FlatXmlDataFileLoader
import org.dbunit.operation.DatabaseOperation
import java.sql.Connection

import scala.collection.JavaConversions._

trait DBSupport {
  lazy val ds : DataSource = {
    val source = new JdbcDataSource()
    source.setURL("jdbc:h2:test")
    source.setUser("sa")
    source.setPassword("")

    source
  }

  def loadData(implicit con:Connection) {
    val dbConn = new DatabaseConnection(con)
    val dataLoader = new FlatXmlDataFileLoader(Map[String, String]("[null]" -> null))
    val files = List("/data.xml", "/artists.xml")

    for (f <- files) {
      val dataset = dataLoader.load(f)
      DatabaseOperation.CLEAN_INSERT.execute(dbConn, dataset)
    }
  }
}
