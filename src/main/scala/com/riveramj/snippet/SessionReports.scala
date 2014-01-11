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

object SessionReports {
  val menu = Menu.i("Session Reports") / "session-reports"
}

class SessionReports extends Loggable {

  def create = {
    var studentId = ""
    var tutorId = ""
    var present = false
    var semester: Box[Semester] = Empty
    var location: Box[Location] = Empty
    var stationSubject: Box[StationSubject] = Empty
    var studentGroup: Box[String] = Empty 
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
    "#group-number" #> SHtml.text("", group =>  studentGroup = Some(group)) &
    "#location" #> locationSelect &
    "#semester" #> semesterSelect &
    "#station-subject" #> stationSubjectSelect &
    "#session-date" #> SHtml.text(date, date = _) &
    "#homework" #> SHtml.text("", hw =>  homework = Some(hw)) &
    "#material-covered" #> SHtml.text("", covered =>  materialCovered = Some(covered)) &
    "#tutor-remarks" #> SHtml.text("", remarks =>  tutorRemarks = Some(remarks)) &
    "#add-report" #> SHtml.ajaxOnSubmit(createReport _)
  }

  def list = {
    val reports = SessionReport.findAll

    def deleteReport(reportId: ObjectId): JsCmd = {
      SessionReport.find(reportId).map(_.delete)
      
      SessionReport.find(reportId) match {
        case None =>
          JsCmds.Run("$('#" + reportId + "').parent().parent().remove()")
        case _ => 
        logger.error(s"couldn't delete student with id $reportId")
      }
    }
    
    ClearClearable andThen
    ".report-entry" #> reports.map { report => 
      val student = Student.find(report.studentId)
      val tutor = Tutor.find(report.tutorId)
      
      ".studentName *" #> student.map(s => s.firstName + " " + s.lastName) &
      ".tutorName *" #> tutor.map(t => t.firstName + " " + t.lastName) &
      ".semester *" #> report.semester.map(_.toString) &
      ".delete-report [id]" #> report._id.toString &
      ".delete-report [onclick]" #> SHtml.ajaxInvoke(() => {
        JsCmds.Confirm("Are you sure you want to delete the report?", {
          SHtml.ajaxInvoke(() => {
            deleteReport(report._id)
          }).cmd
        })
      })
    }
  }

}
