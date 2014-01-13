package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import net.liftweb.util.ClearClearable
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.common._
import org.bson.types.ObjectId

import java.util.Date

import com.riveramj.model._

object CreateSessionReports {
  val menu = Menu.i("Create Session Reports") / "create-session-report"
}

class CreateSessionReports extends Loggable {

  def render = {
    var studentId = ""
    var tutorId = ""
    var present = false
    var semester: Box[Semester] = Empty
    var location: Box[Location] = Empty
    var stationSubject: Box[StationSubject] = Empty
    var stationNumber: Box[String] = Empty 
    var homework: Box[String] = Empty 
    var materialCovered: Box[String] = Empty
    var tutorRemarks: Box[String] = Empty
    var sessionDate = ""
    var date = ""

    def createReport(): JsCmd = {
      val report = SessionReport(
        _id = ObjectId.get,
        studentId = ObjectId.massageToObjectId(studentId),
        present = present,
        tutorId = ObjectId.massageToObjectId(tutorId),
        semester = semester,
        location = location,
        date = new Date(),
        stationSubject = stationSubject,
        stationNumber = stationNumber,
        homework = homework,
        materialCovered = materialCovered,
        tutorRemarks = tutorRemarks
      )

      report.save

      SessionReport.find(report._id) match {
        case Some(report) => 
          RedirectTo(  
            SessionReports.menu.loc.calcDefaultHref
          )
          
        case error =>
          logger.error(s"error creating report: $error")

          JsCmds.Alert("Internal error. Please try again.")
      }
    }

    val allStudents = Student.findAll
    val allTutors = Tutor.findAll
    val allSemesters = Semester.all
    val allLocations = Location.all
    val allStationSubjects = StationSubject.all
    
    def studentSelect = {
      SHtml.select(
        allStudents.sortWith(_.lastName < _.lastName).map(student => (student._id.toString, student.firstName)),
        Full(""),
        studentId = _
      )
    }

    def tutorSelect = {
      SHtml.select(
        allTutors.sortWith(_.lastName < _.lastName).map(tutor => (tutor._id.toString, tutor.firstName)),
        Full(""),
        tutorId = _
      )
    }

    def locationSelect = {
      SHtml.selectObj(
        allLocations.toList.map(location => (Full(location), location.toString)),
        Full(Empty),
        (selectedLocation: Box[Location]) => location = selectedLocation
      )
    }

    def semesterSelect = {
      SHtml.selectObj(
        allSemesters.toList.map(semester => (Full(semester), semester.toString)),
        Full(Empty),
        (selectedSemester: Box[Semester]) => semester = selectedSemester
      )
    }

    def stationSubjectSelect = {
      SHtml.selectObj(
        allStationSubjects.toList.map(subject => (Full(subject), subject.toString)),
        Full(Empty),
        (selectedSubject: Box[StationSubject]) => stationSubject = selectedSubject
      )
    }

    SHtml.makeFormsAjax andThen
    "#student" #> studentSelect &
    "#student-present" #> SHtml.checkbox(present, present = _) &
    "#tutor" #> tutorSelect &
    "#station-number" #> SHtml.text("", station =>  stationNumber = Some(station)) &
    "#location" #> locationSelect &
    "#semester" #> semesterSelect &
    "#station-subject" #> stationSubjectSelect &
    "#session-date" #> SHtml.text(date, date = _) &
    "#homework" #> SHtml.textarea("", hw =>  homework = Some(hw)) &
    "#material-covered" #> SHtml.textarea("", covered =>  materialCovered = Some(covered)) &
    "#tutor-remarks" #> SHtml.textarea("", remarks =>  tutorRemarks = Some(remarks)) &
    "#add-report" #> SHtml.ajaxOnSubmit(createReport _)
  }

}
