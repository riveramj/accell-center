package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import net.liftweb.util.ClearClearable
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.common._
import org.bson.types.ObjectId

import com.riveramj.model.Tutor

object Tutors {
  val menu = Menu.i("tutors") / "tutors"
}

class Tutors extends Loggable {

  def create = {
    var firstName = ""
    var lastName = ""
    var email = ""
    var phone: Option[String] = Empty 

    def createTutor(): JsCmd = {
      val tutor = Tutor(
        _id = ObjectId.get,
        firstName = firstName,
        lastName = lastName,
        email = email,
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
    "#phone" #> SHtml.text("", number =>  phone = Some(number)) &
    "#add-tutor" #> SHtml.ajaxOnSubmit(createTutor _)
  }

  def list = {
    val tutors = Tutor.findAll

    def deleteTutor(tutorId: ObjectId): JsCmd = {
      Tutor.find(tutorId).map(_.delete)
      
      Tutor.find(tutorId) match {
        case None =>
          JsCmds.Run("$('#" + tutorId + "').parent().parent().remove()")
        case _ => 
        logger.error(s"couldn't delete tutor with id $tutorId")
      }
    }
    
    ClearClearable andThen
    ".tutor-row" #> tutors.map { tutor => 
      ".name *" #> (tutor.firstName + " " + tutor.lastName) &
      ".email *" #> tutor.email &
      ".phone *" #> tutor.phone & 
      ".delete-tutor [id]" #> tutor._id.toString &
      ".delete-tutor [onclick]" #> SHtml.ajaxInvoke(() => {
        JsCmds.Confirm("Are you sure you want to delete the tutor?", {
          SHtml.ajaxInvoke(() => {
            deleteTutor(tutor._id)
          }).cmd
        })
      })
    }
  }
}
