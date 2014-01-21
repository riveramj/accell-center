package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._

case class SessionReport(
  _id: ObjectId,
  studentId: ObjectId,
  present: Boolean,
  tutorRemarks: Option[String] = None,
  sessionReportGroupDetailsId: ObjectId 
)
  extends MongoDocument[SessionReport] {
  def meta = SessionReport
}

object SessionReport extends MongoDocumentMeta[SessionReport] {
  override def collectionName = "sessionReport"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer
}


