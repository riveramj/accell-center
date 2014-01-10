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

sealed trait Semester
object Semester {
  case object spring2014 extends Semester
  case object summer2014 extends Semester
  case object fall2014   extends Semester
  case object sprint2015 extends Semester
  case object summer2015 extends Semester
  case object fall2015   extends Semester
}

case class SessionReport(
  _id: ObjectId,
  studentId: ObjectId,
  tutorId: ObjectId,
  present: Boolean,
  group: Option[String] = None,
  semester: Semester,
  location: Location,
  date: Date,
  homework: Option[String] = None,
  materialCovered: Option[String] = None
)
  extends MongoDocument[SessionReport] {
  def meta = SessionReport
}

object SessionReport extends MongoDocumentMeta[SessionReport] {
  override def collectionName = "sessionReport"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer +
    new LocationTypeSerializer + new DateSerializer + new SemesterTypeSerializer
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

class SemesterTypeSerializer extends Serializer[Semester] {

  private val SemesterClass = classOf[Semester]

  def deserialize(implicit format: Formats) = {

    case (TypeInfo(SemesterClass, _), json) =>
      val className = json.extract[String]
      Class.forName(className).getField("MODULE$").get().asInstanceOf[Semester]
  }

  def serialize(implicit format: Formats) = {
    case possibleSemester if SemesterClass.isInstance(possibleSemester) &&
      possibleSemester.getClass.getName().endsWith("$") => possibleSemester.getClass.getName()
  }
}
