package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import net.liftweb.util.ClearClearable
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.common._
import org.bson.types.ObjectId

import scala.xml.NodeSeq

import com.riveramj.model.Tutor
import com.riveramj.model.Subject

object Tutors {
  val menu = Menu.i("tutors") / "tutors"
}

class Tutors extends Loggable {

  def create = {
    var firstName = ""
    var lastName = ""
    var email = ""
    var mathSubject = false
    var englishSubject = false
    var phone: Option[String] = Empty 

    def createTutor(): JsCmd = {

      val selectedSubjects: List[Subject] = {
       (if (mathSubject) List(Subject.Math) else Nil) ++
       (if (englishSubject) List(Subject.English) else Nil)
      }

      val tutor = Tutor(
        _id = ObjectId.get,
        firstName = firstName,
        lastName = lastName,
        email = email,
        subjects = selectedSubjects,
        phone = phone
      )

      tutor.save

      Tutor.find(tutor._id) match {
        case Some(tutor) => 
          RedirectTo(  
            Tutors.menu.loc.calcDefaultHref
          )
          
        case error =>
          logger.error(s"error creating tutor: $error")

          JsCmds.Alert("Internal error. Please try again.")
      }
    }

    SHtml.makeFormsAjax andThen
    "#first-name" #> SHtml.text(firstName, firstName = _) &
    "#last-name" #> SHtml.text(lastName, lastName = _) &
    "#email" #> SHtml.text(email, email = _) &
    "#phone" #> SHtml.text("", number => phone = Some(number)) &
    "#math-subject" #> SHtml.checkbox(false, mathSubject = _, ("id", "math-subject")) &
    "#english-subject" #> SHtml.checkbox(false, englishSubject = _, ("id", "english-subject")) &
    "#add-tutor" #> SHtml.ajaxOnSubmit(createTutor _)
  }

  def list = {
    val tutors = Tutor.findAll

    def deleteTutor(tutorId: ObjectId): JsCmd = {
      Tutor.find(tutorId).map(_.delete)
      
      Tutor.find(tutorId) match {
        case None =>
          JsCmds.Run("$('#" + tutorId + "').parent().parent().prev().remove()") &
          JsCmds.Run("$('#" + tutorId + "').parent().parent().remove()")
        case _ => 
        logger.error(s"couldn't delete tutor with id $tutorId")
      }
    }

    def renderTutor(rows: NodeSeq, tutor: Tutor) = {
      {
        ".name *" #> (tutor.firstName + " " + tutor.lastName) &
        ".email *" #> tutor.email &
        ".phone *" #> tutor.phone &
        ".subjects *" #> tutor.subjects.map(_.toString).mkString(", ") &
        ".tutor-details [id]" #> tutor._id.toString &
        ".summary [data-target]" #> ("#" + tutor._id.toString) &
        ".edit-tutor [href]" #> ("/tutors/edit/" + tutor._id.toString) &
        ".delete-tutor [onclick]" #> SHtml.ajaxInvoke(() => {
          JsCmds.Confirm("Are you sure you want to delete the tutor?", {
            SHtml.ajaxInvoke(() => {
              deleteTutor(tutor._id)
            }).cmd
          })
        })
      } apply rows
    }
    
    ClearClearable andThen
    "tbody *" #> { rows: NodeSeq =>
      val summaryRow = (".summary ^^" #> "unused") apply rows 
      val detailsRow = (".details ^^" #> "unused") apply rows

      tutors.map { tutor =>
        renderTutor(summaryRow ++ detailsRow, tutor)
      }.flatten
    }
  }
}
