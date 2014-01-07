package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._
import java.util.Date
import net.liftweb.json.{TypeInfo, Formats, Serializer}
import net.liftweb.json.JsonDSL._

sealed trait Location
object Location {
  case object cityOfRefuge extends Location
  case object other extends Location
}

case class StudentEvaluation(
  _id: ObjectId,
  studentId: ObjectId,
  present: Boolean,
  group: Option[String] = None,
  semester: String,
  location: Location,
  date: Date,
  homework: Option[String] = None,
  sectionsCovered: Option[String] = None, 
  tutorId: ObjectId
)
  extends MongoDocument[StudentEvaluation] {
  def meta = StudentEvaluation
}

object StudentEvaluation extends MongoDocumentMeta[StudentEvaluation] {
  override def collectionName = "studentEvaluation"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer +
    new LocationTypeSerializer + new DateSerializer
}


class LocationTypeSerializer extends Serializer[Location] {

  private val LocationClass = classOf[Location]

  def deserialize(implicit format: Formats) = {

    case (TypeInfo(LocationClass, _), json) =>
      val className = json.extract[String]
      Class.forName(className).getField("MODULE$").get().asInstanceOf[Location]
  }

  def serialize(implicit format: Formats) = {
    case possibleLocation if LocationClass.isInstance(possibleLocation) &&
      possibleLocation.getClass.getName().endsWith("$") => possibleLocation.getClass.getName()
  }
}
