package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._

import net.liftweb.json.{TypeInfo, Formats, Serializer}
import net.liftweb.json.JsonDSL._

sealed trait Progression
object Progression {
  case object Advance extends Progression 
  case object Retain extends Progression

  lazy val all:Set[Progression] = Set(
    Advance,
    Retain
  )
}

case class SessionReport(
  _id: ObjectId,
  studentId: ObjectId,
  present: Boolean,
  progression: Option[Progression] = None,
  sessionRemarks: Option[String] = None,
  homeworkRemarks: Option[String] = None,
  sessionReportGroupDetailsId: ObjectId 
)
  extends MongoDocument[SessionReport] {
  def meta = SessionReport
}

object SessionReport extends MongoDocumentMeta[SessionReport] {
  override def collectionName = "sessionReport"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer + new ProgressionSerializer
}

class ProgressionSerializer extends Serializer[Progression] {

  private val ProgressionClass = classOf[Progression]

  def deserialize(implicit format: Formats) = {

    case (TypeInfo(ProgressionClass, _), json) =>
      val className = json.extract[String]
      Class.forName(className).getField("MODULE$").get().asInstanceOf[Progression]
  }

  def serialize(implicit format: Formats) = {
    case possibleProgression if ProgressionClass.isInstance(possibleProgression) &&
      possibleProgression.getClass.getName().endsWith("$") => possibleProgression.getClass.getName()
  }
}


