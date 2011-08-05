package no.ovstetun

import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource

trait DBSupport {
  lazy val ds : DataSource = {
    val source = new JdbcDataSource()
    source.setURL("jdbc:h2:test")
    source.setUser("sa")
    source.setPassword("")

    source
  }
}
