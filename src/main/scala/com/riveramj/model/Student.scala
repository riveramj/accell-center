package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._

case class Student(
  _id: ObjectId,
  firstName: String,
  lastName: String,
  studentId: String,
  email: Option[String] = None,
  phone: Option[String] = None,
  parents: Option[String] = None
)
  extends MongoDocument[Student] {
  def meta = Student
}

object Student extends MongoDocumentMeta[Student] {
  override def collectionName = "student"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer
}
