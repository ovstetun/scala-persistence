package no.ovstetun.jpa

import org.eclipse.persistence.mappings.converters.Converter
import org.eclipse.persistence.mappings.DatabaseMapping
import org.eclipse.persistence.sessions.Session
import no.ovstetun.Genre

class GenreConverter extends Converter {
  def convertObjectValueToDataValue(objectValue: AnyRef, session: Session) = {
    val g = objectValue.asInstanceOf[Genre.Genre]
    g.id.asInstanceOf[AnyRef]
  }

  def convertDataValueToObjectValue(dataValue: AnyRef, session: Session) = {
    Genre(dataValue.asInstanceOf[Int])
  }

  def isMutable = false

  def initialize(mapping: DatabaseMapping, session: Session) {}
}
