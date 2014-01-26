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

import scala.xml.NodeSeq

object EditStudent {
  import net.liftweb.sitemap._
    import Loc._
  import com.riveramj.util.Paths._

  val menu = MenuId("Create Student") / "students" / "edit" / * >>
  TemplateBox(() => Templates("edit-student" :: Nil))
}

class EditStudent extends Loggable {

  val editingStudentId = EditStudent.menu.loc.currentValue openOr ""
  val student = 
    Student.find(ObjectId.massageToObjectId(editingStudentId)) match {
      case Some(possibleStudent) => 
        possibleStudent
      case None => 
        Student(_id = ObjectId.get, firstName = "", lastName = "", studentId ="") 
    }


  def render = {
    var firstName = ""
    var lastName = ""
    var email: Option[String] = Empty 
    var phone: Option[String] = Empty
    var parents: Option[String] = Empty

    def createStudent(): JsCmd = {
      val updatedStudent = student.copy(
        firstName = firstName,
        lastName = lastName,
        studentId = "",
        email = email,
        phone = phone,
        parents = parents
      )

      updatedStudent.save

      Student.find(updatedStudent._id) match {
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
    "#first-name" #> SHtml.text(student.firstName, firstName = _) &
    "#last-name" #> SHtml.text(student.lastName, lastName = _) &
    "#email" #> SHtml.text(student.email.getOrElse(""), emailAddress =>  email = Some(emailAddress)) &
    "#phone" #> SHtml.text(student.phone.getOrElse(""), number =>  phone = Some(number)) &
    "#parents" #> SHtml.text(student.parents.getOrElse(""), parentsNames =>  parents = Some(parentsNames)) &
    "#add-student" #> SHtml.ajaxOnSubmit(createStudent _)
  }
}
