package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._

import net.liftweb.json.{TypeInfo, Formats, Serializer}
import net.liftweb.json.JsonDSL._

sealed trait Subject
object Subject {
  case object Math extends Subject 
  case object English extends Subject

  lazy val all:Set[Subject] = Set(
    Math,
    English
  )
}

case class Tutor(
  _id: ObjectId,
  firstName: String,
  lastName: String,
  email: String,
  subjects: List[Subject] = Nil,
  phone: Option[String] = None
)
  extends MongoDocument[Tutor] {
  def meta = Tutor
}

object Tutor extends MongoDocumentMeta[Tutor] {
  override def collectionName = "tutor"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer + new SubjectSerializer
}

class SubjectSerializer extends Serializer[Subject] {

  private val SubjectClass = classOf[Subject]

  def deserialize(implicit format: Formats) = {

    case (TypeInfo(SubjectClass, _), json) =>
      val className = json.extract[String]
      Class.forName(className).getField("MODULE$").get().asInstanceOf[Subject]
  }

  def serialize(implicit format: Formats) = {
    case possibleSubject if SubjectClass.isInstance(possibleSubject) &&
      possibleSubject.getClass.getName().endsWith("$") => possibleSubject.getClass.getName()
  }
}
