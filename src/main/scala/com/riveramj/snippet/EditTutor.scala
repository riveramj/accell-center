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

object EditTutor {
  import net.liftweb.sitemap._
    import Loc._
  import com.riveramj.util.Paths._

  val menu = MenuId("Create Tutor") / "tutors" / "edit" / * >>
  TemplateBox(() => Templates("create-tutor" :: Nil))
}

class EditTutor extends Loggable {
  val editingTutorId = EditTutor.menu.loc.currentValue openOr ""
  val tutor = 
    Tutor.find(ObjectId.massageToObjectId(editingTutorId)) match {
      case Some(possibleTutor) => 
        possibleTutor
      case None => 
        Tutor(_id = ObjectId.get, firstName = "", lastName = "", email ="") 
    }

  def render = {
    var firstName = ""
    var lastName = ""
    var email = ""
    var mathSubject = tutor.subjects.contains(Subject.Math)
    var englishSubject = tutor.subjects.contains(Subject.English)
    var phone: Option[String] = Empty 

    def createTutor(): JsCmd = {

      val selectedSubjects: List[Subject] = {
        (if (mathSubject) List(Subject.Math) else Nil) ++
        (if (englishSubject) List(Subject.English) else Nil)
      }

      val updatedTutor = tutor.copy(
        firstName = firstName,
        lastName = lastName,
        email = email,
        subjects = selectedSubjects,
        phone = phone
      )

      updatedTutor.save

      Tutor.find(ObjectId.massageToObjectId(tutor._id)) match {
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
    "#first-name" #> SHtml.text(tutor.firstName, firstName = _) &
    "#last-name" #> SHtml.text(tutor.lastName, lastName = _) &
    "#email" #> SHtml.text(tutor.email, email = _) &
    "#phone" #> SHtml.text(tutor.phone.getOrElse(""), number => phone = Some(number)) &
    "#math-subject" #> SHtml.checkbox(mathSubject, mathSubject = _, ("id", "math-subject")) &
    "#english-subject" #> SHtml.checkbox(englishSubject, englishSubject = _, ("id", "english-subject")) &
    "#save-tutor" #> SHtml.ajaxOnSubmit(createTutor _)
  }
}
