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

object Students {
  val menu = Menu.i("students") / "students"
}

class Students extends Loggable {

  def list = {
    val students = Student.findAll

    def deleteStudent(studentId: ObjectId): JsCmd = {
      Student.find(studentId).map(_.delete)
      
      Student.find(studentId) match {
        case None =>
          JsCmds.Run("$('#" + studentId + "').parent().parent().prev().remove()") &
          JsCmds.Run("$('#" + studentId + "').parent().parent().remove()")
        case _ => 
        logger.error(s"couldn't delete student with id $studentId")
      }
    }

    def renderStudent(rows: NodeSeq, student: Student) = {
      {
        ".name *" #> (student.firstName + " " + student.lastName) &
        ".email *" #> student.email &
        ".phone *" #> student.phone &
        ".parents *" #> student.parents &
        ".student-details [id]" #> student._id.toString &
        ".summary [data-target]" #> ("#" + student._id.toString) &
        ".edit-student [href]" #> ("/students/edit/" + student._id.toString) &
        ".delete-student [onclick]" #> SHtml.ajaxInvoke(() => {
          JsCmds.Confirm("Are you sure you want to delete the student?", {
            SHtml.ajaxInvoke(() => {
              deleteStudent(student._id)
            }).cmd
          })
        })
      } apply rows
    }

    ClearClearable andThen
    "tbody *" #> { rows: NodeSeq =>
      val summaryRow = (".summary ^^" #> "unused") apply rows 
      val detailsRow = (".details ^^" #> "unused") apply rows

      students.map { student =>
        renderStudent(summaryRow ++ detailsRow, student)
      }.flatten
    }
  }
}
