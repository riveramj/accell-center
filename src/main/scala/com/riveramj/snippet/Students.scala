package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import net.liftweb.util.ClearClearable
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.common._
import org.bson.types.ObjectId

import com.riveramj.model.Student

object Students {
  val menu = Menu.i("students") / "students"
}

class Students extends Loggable {

  def create = {
    var firstName = ""
    var lastName = ""
    var email: Option[String] = Empty 
    var phone: Option[String] = Empty
    var parents: Option[String] = Empty

    def createStudent(): JsCmd = {
      val student = Student(
        _id = ObjectId.get,
        firstName = firstName,
        lastName = lastName,
        studentId = "",
        email = email,
        phone = phone,
        parents = parents
      )

      student.save

      Student.find(student._id) match {
        case Some(student) => 
          RedirectTo(  
            Students.menu.loc.calcDefaultHref
          )
          
        case error =>
          logger.error(s"error creating student: $error")

          JsCmds.Alert("Internal error. Please try again.")
      }
    }

    SHtml.makeFormsAjax andThen
    "#first-name" #> SHtml.text(firstName, firstName = _) &
    "#last-name" #> SHtml.text(lastName, lastName = _) &
    "#email" #> SHtml.text("", emailAddress =>  email = Some(emailAddress)) &
    "#phone" #> SHtml.text("", number =>  phone = Some(number)) &
    "#parents" #> SHtml.text("", parentsNames =>  parents = Some(parentsNames)) &
    "#add-student" #> SHtml.ajaxOnSubmit(createStudent _)
  }

  def list = {
    val students = Student.findAll

    def deleteStudent(studentId: ObjectId): JsCmd = {
      Student.find(studentId).map(_.delete)
      
      Student.find(studentId) match {
        case None =>
          JsCmds.Run("$('#" + studentId + "').parent().parent().remove()")
        case _ => 
        logger.error(s"couldn't delete student with id $studentId")
      }
    }
    
    ClearClearable andThen
    ".student-row" #> students.map { student => 
      ".name *" #> (student.firstName + " " + student.lastName) &
      ".email *" #> student.email &
      ".phone *" #> student.phone &
      ".parents *" #> student.parents &
      ".delete-student [id]" #> student._id.toString &
      ".delete-student [onclick]" #> SHtml.ajaxInvoke(() => {
        JsCmds.Confirm("Are you sure you want to delete the student?", {
          SHtml.ajaxInvoke(() => {
            deleteStudent(student._id)
          }).cmd
        })
      })
    }
  }
}
