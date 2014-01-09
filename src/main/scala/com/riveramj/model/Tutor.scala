package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._

case class Tutor(
  _id: ObjectId,
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String] = None
)
  extends MongoDocument[Tutor] {
  def meta = Tutor
}

object Tutor extends MongoDocumentMeta[Tutor] {
  override def collectionName = "tutor"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer
}
