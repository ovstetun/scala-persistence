package no.ovstetun.jpa

import org.specs2.mutable.{After, Specification}
import java.sql.Connection
import org.specs2.specification.{Step, Fragments}
import org.scala_libs.jpa.{ScalaEMFactory, ScalaEntityManager}
import javax.persistence.{EntityManager, Persistence}

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

    val RichEM = new RichEM(em)

    def after {
      em.getTransaction.rollback()
      em.close()
    }
  }
  class RichEM(val em:EntityManager) extends ScalaEntityManager {
    val factory = new ScalaEMFactory {
      def closeEM(em: EntityManager) {em.close}
      def getUnitName = "pu"
      def openEM() = em
    }
  }
}
