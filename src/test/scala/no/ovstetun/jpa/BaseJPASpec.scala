package no.ovstetun.jpa

import org.specs2.mutable.{After, Specification}
import java.sql.Connection
import javax.persistence.Persistence
import org.specs2.specification.{Step, Fragments}

trait BaseJPASpec extends Specification {
  lazy val emf = Persistence.createEntityManagerFactory("pu")

  override def map(fs: =>Fragments) = fs ^ Step(closeEMF)

  def closeEMF {
    emf.close()
  }

  trait t extends After {
    val em = emf.createEntityManager()
    em.getTransaction.begin()

    implicit def conn = em.unwrap(classOf[Connection])

    def after {
      em.getTransaction.rollback()
      em.close()
    }
  }
}
