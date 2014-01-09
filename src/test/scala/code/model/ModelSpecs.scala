package code.model

import org.specs2.mutable.Specification
import bootstrap.liftweb.Boot
import com.riveramj.model._
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Extraction
import java.util.Date

class ModelSpecs extends Specification {

  step {
    val boot = new Boot
    boot.boot
  }

  val student1Id = "001"
  val student1 = Student(
    _id = ObjectId.get,
    firstName = "Mike",
    lastName = "Rivera",
    studentId = student1Id
  )

  val tutor1Id = "001"
  val tutor1 = Tutor(
    _id = ObjectId.get,
    firstName = "Mike",
    lastName = "Rivera",
    tutorId = tutor1Id
  )

  val studentEvaluation1 = StudentEvaluation(
    _id = ObjectId.get,
    studentId = student1._id,
    present = true,
    group = Some("2"),
    semester = "Spring 2014",
    location = Location.cityOfRefuge,
    date = new Date(),
    homework = Some("p456 #2,3,4,5,6"),
    sectionsCovered = Some("Sections 9a, 9b, 9c. Not 9d"),
    tutorId = tutor1._id
  )

  "the student should" should {
    args(sequential=true)
    
    "create test student" in {
      student1.save
      Student.find(student1._id).get must beEqualTo(student1)
    }
    "find unique student by studentId" in {
      val student = Student.findAll("studentId" -> student1Id)
    
      student.length must beEqualTo(1)
      student.head.studentId must beEqualTo(student1Id)
    }
    "delete student by id" in {
      val student = Student.find(student1._id).get
      student.delete
    
      Student.find(student1._id) must beEmpty
    }
  }
  "the tutor should" should {
    args(sequential=true)

    "create test tutor" in {
      tutor1.save
      Tutor.find(tutor1._id).get must beEqualTo(tutor1)
    }
    "find unique tutor by tutorId" in {
      val tutor = Tutor.findAll("tutorId" -> tutor1Id)
    
      tutor.length must beEqualTo(1)
      tutor.head.tutorId must beEqualTo(tutor1Id)
    }
    "delete tutor by id" in {
      val tutor = Tutor.find(tutor1._id).get
      tutor.delete
    
      Tutor.find(tutor1._id) must beEmpty
    }
  }
  "the student evaluation should" should {
    args(sequential=true)

    "create test student" in {
      studentEvaluation1.save
      StudentEvaluation.find(studentEvaluation1._id).get must beEqualTo(studentEvaluation1)
    }
    "delete student evaluation by id" in {
      val studentEvaluation = StudentEvaluation.find(studentEvaluation1._id).get
      studentEvaluation.delete
    
      StudentEvaluation.find(studentEvaluation1._id) must beEmpty
    }
  }
}