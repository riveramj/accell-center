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

case class StudentReport(
  id: ObjectId,
  studentId:String = "", 
  present:Boolean = false, 
  tutorRemarks:String = ""
)

class CreateSessionReports extends Loggable {

  def render = {
    var tutorId = ""
    var semester: Box[Semester] = Empty
    var location: Box[Location] = Empty
    var stationSubject: Box[StationSubject] = Empty
    var stationNumber: Box[String] = Empty 
    var homework: Box[String] = Empty 
    var materialCovered: Box[String] = Empty
    var sessionDate = ""
    var date = ""
    var students = Seq(StudentReport(id = ObjectId.get))

    val allStudents = Student.findAll
    val allTutors = Tutor.findAll
    val allSemesters = Semester.all
    val allLocations = Location.all
    val allStationSubjects = StationSubject.all

    def saveStudents(groupReportId: ObjectId) = {
      val savedStudents = students.map { studentReport =>
        val report = SessionReport(
          _id = ObjectId.get,
          studentId = ObjectId.massageToObjectId(studentReport.studentId),
          present = studentReport.present,
          tutorRemarks = emptyForBlank(studentReport.tutorRemarks),
          sessionReportGroupDetailsId = groupReportId
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

      savedStudents.head
    }
   
    def createReport: JsCmd = {
      val reportGroupDetails = SessionReportGroupDetails(
        _id = ObjectId.get,
        tutorId = ObjectId.massageToObjectId(tutorId),
        semester = semester,
        location = location,
        date = new Date(),
        stationSubject = stationSubject,
        stationNumber = stationNumber,
        homework = homework,
        materialCovered = materialCovered
      )

      reportGroupDetails.save

      SessionReportGroupDetails.find(reportGroupDetails._id) match {
        case Some(report) => 
          saveStudents(report._id)

        case error =>
          logger.error(s"error creating report: $error")

          JsCmds.Alert("Internal error. Please try again.")
      }
    }

    def updateStudent(selectedStudent: String, id: ObjectId) = {
      students = students.collect {
        case report if report.id == id =>
          report.copy(studentId = selectedStudent)
        case report => 
          report
      }
    }

    def updatePresent(studentPresent: Boolean, id: ObjectId) = {
      students = students.collect {
        case report if report.id == id =>
          report.copy(present = studentPresent)
        case report => 
          report
      }
    }

    def updateRemarks(remarks: String, id: ObjectId) = {
      students = students.collect {
        case report if report.id == id =>
          report.copy(tutorRemarks = remarks)
        case report => 
          report
      }
    }

    def addStudent(renderer: IdMemoizeTransform)() = {
      students = students :+ StudentReport(id = ObjectId.get)
      
      renderer.setHtml()
    }
    

    def renderStudentReports(report: StudentReport) = {

      def studentSelect = {
        SHtml.select(
          allStudents.sortWith(_.lastName < _.lastName).map(student => (student._id.toString, student.firstName)),
          Full(report.studentId),
          updateStudent(_, report.id)
        )
      }

      ".student" #> studentSelect &
      ".present" #> SHtml.checkbox(
        report.present, 
        updatePresent(_, report.id)
      ) &
      ".remarks" #> SHtml.textarea(
        report.tutorRemarks, 
        updateRemarks(_, report.id)
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
    "#tutor" #> tutorSelect &
    "#station-number" #> SHtml.text("", station =>  stationNumber = Some(station)) &
    "#location" #> locationSelect &
    "#semester" #> semesterSelect &
    "#station-subject" #> stationSubjectSelect &
    "#session-date" #> SHtml.text(date, date = _) &
    "#homework" #> SHtml.textarea("", hw =>  homework = Some(hw)) &
    "#material-covered" #> SHtml.textarea("", covered =>  materialCovered = Some(covered)) &
    ".students" #> SHtml.idMemoize { renderer =>
      ".student-entry" #> students.map(renderStudentReports(_)) &
      ".add-student" #> SHtml.ajaxOnSubmit(addStudent(renderer)) &
      "#create-report" #> SHtml.ajaxOnSubmit(createReport _)
    }
  }
}
