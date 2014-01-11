package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._
import java.util.Date
import net.liftweb.json.{TypeInfo, Formats, Serializer}
import net.liftweb.json.JsonDSL._

sealed trait Location
object Location {
  case object CityOfRefuge extends Location {
    override def toString() = "City of Refuge"
  }
  case object Other extends Location

  lazy val all:Set[Location] = Set(
    CityOfRefuge,
    Other
  )
}


sealed trait Semester
object Semester {
  case object Spring2014 extends Semester {
    override def toString() = "Spring 2014"
  }
  case object Summer2014 extends Semester {
    override def toString() = "Summer 2014"
  }
  case object Fall2014 extends Semester {
    override def toString() = "Fall 2014"
  }
  case object Spring2015 extends Semester {
    override def toString() = "Spring 2015"
  }
  case object Summer2015 extends Semester {
    override def toString() = "Summer 2015"
  }
  case object Fall2015 extends Semester {
    override def toString() = "Fall 2015"
  }

  lazy val all:Set[Semester] = Set(
    Spring2014,
    Summer2014,
    Fall2014,
    Spring2015,
    Summer2015,
    Fall2015
  )
}

  )
}

case class SessionReport(
  _id: ObjectId,
  studentId: ObjectId,
  tutorId: ObjectId,
  present: Boolean,
  group: Option[String] = None,
  semester: Option[Semester],
  location: Option[Location],
  date: Date,
  homework: Option[String] = None,
  materialCovered: Option[String] = None,
  tutorRemarks: Option[String] = None
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
