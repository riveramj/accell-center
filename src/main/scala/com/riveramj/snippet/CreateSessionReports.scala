package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import net.liftweb.util.ClearClearable
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.common._
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JObject

import java.util.Date
import java.text.SimpleDateFormat

import com.riveramj.model._

object EditSessionReport {
  import net.liftweb.sitemap._
    import Loc._
  import com.riveramj.util.Paths._

  val menu = MenuId("Create Session Reports") / "session-report" / "edit" / * >>
  TemplateBox(() => Templates("edit-session-report" :: Nil))
}

class EditSessionReport extends Loggable {
  val editingReportId = EditSessionReport.menu.loc.currentValue openOr ""
  val possibleDetails = SessionReportGroupDetails.find(ObjectId.massageToObjectId(editingReportId))
  val groupDetails = possibleDetails match {
      case Some(details) => 
        details
      case None => 
        SessionReportGroupDetails(
          _id = ObjectId.get, 
          tutorId = null, 
          stationSubject = None,
          semester = None,
          location = None,
          date = new Date()
        ) 
    }
  val studentReports = SessionReport.findAll("sessionReportGroupDetailsId" -> ("$oid" -> groupDetails._id.toString))

  def reportPlaceholder = {
    SessionReport(
      _id = ObjectId.get,
      studentId = null,
      present = false,
      sessionReportGroupDetailsId = null
    )
  }

  def render = {
    var tutorId = ""
    var semester: Box[Semester] = Empty
    var location: Box[Location] = Empty
    var stationSubject: Box[StationSubject] = Empty
    var stationNumber: Box[String] = Empty 
    var homework: Box[String] = Empty 
    var materialCovered: Box[String] = Empty
    var sessionDate = ""
    var students = 
      if(studentReports.isEmpty)
         List(reportPlaceholder)
      else
        studentReports

    val allStudents = Student.findAll
    val allTutors = Tutor.findAll
    val allSemesters = Semester.all
    val allLocations = Location.all
    val allStationSubjects = StationSubject.all

    def saveStudents(groupReportId: ObjectId) = {
      val savedStudents = students.map { studentReport =>
        val updatedReport = studentReport.copy(
          studentId = ObjectId.massageToObjectId(studentReport.studentId),
          present = studentReport.present,
          progression = studentReport.progression,
          sessionRemarks = studentReport.sessionRemarks,
          homeworkRemarks =  studentReport.homeworkRemarks,
          sessionReportGroupDetailsId = groupReportId
        )

        updatedReport.save

        SessionReport.find(updatedReport._id) match {
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
      val updatedGroupDetails = groupDetails.copy(
        tutorId = ObjectId.massageToObjectId(tutorId),
        semester = semester,
        location = location,
        date = new SimpleDateFormat("MM/dd/yyyy").parse(sessionDate),
        stationSubject = stationSubject,
        stationNumber = stationNumber,
        homework = homework,
        materialCovered = materialCovered
      )

      updatedGroupDetails.save

      SessionReportGroupDetails.find(updatedGroupDetails._id) match {
        case Some(report) => 
          saveStudents(report._id)

        case error =>
          logger.error(s"error creating report: $error")

          JsCmds.Alert("Internal error. Please try again.")
      }
    }

    def updateStudent(selectedStudent: String, id: ObjectId) = {
      students = students.collect {
        case report if report._id == id =>
          report.copy(studentId = ObjectId.massageToObjectId(selectedStudent))
        case report => 
          report
      }
    }

    def updateProgression(selectedProgression: Box[Progression], id: ObjectId) = {
      students = students.collect {
        case report if report._id == id =>
          report.copy(progression = selectedProgression)
        case report => 
          report
      }
    }

    def updatePresent(studentPresent: Boolean, id: ObjectId) = {
      students = students.collect {
        case report if report._id == id =>
          report.copy(present = studentPresent)
        case report => 
          report
      }
    }

    def updateSessionRemarks(remarks: String, id: ObjectId) = {
      students = students.collect {
        case report if report._id == id =>
          report.copy(sessionRemarks = Some(remarks))
        case report => 
          report
      }
    }

    def updateHomeworkRemarks(remarks: String, id: ObjectId) = {
      students = students.collect {
        case report if report._id == id =>
          report.copy(homeworkRemarks = Some(remarks))
        case report => 
          report
      }
    }

    def addStudent(renderer: IdMemoizeTransform)() = {
      students = students :+ reportPlaceholder
      
      renderer.setHtml()
    }
    

    def renderStudentReports(report: SessionReport) = {
      val studentList = ("","") +: allStudents.sortBy(student => (student.lastName, student.firstName)).map(student => (student._id.toString, (student.firstName + " " + student.lastName)))
        
      def studentSelect = {
        val id = report.studentId match {
          case null => ""
          case possibleId => possibleId.toString
        }
          
        SHtml.select(
          studentList,
          Full(id),
          updateStudent(_, report._id)
        )
      }
      
      def progressionSelect = {
        SHtml.selectObj(
          (Empty, "") +: Progression.all.toList.map(progression => (Full(progression), progression.toString)),
          Full(report.progression:Box[Progression]),
          (selectedProgression: Box[Progression]) => updateProgression(selectedProgression, report._id)
        )
      }

      ".student" #> studentSelect &
      ".present" #> SHtml.checkbox(
        report.present, 
        updatePresent(_, report._id)
      ) &
      ".progression" #> progressionSelect &
      ".session-remarks" #> SHtml.textarea(
        report.sessionRemarks.getOrElse(""), 
        updateSessionRemarks(_, report._id)
      ) &
      ".homework-remarks" #> SHtml.textarea(
        report.homeworkRemarks.getOrElse(""), 
        updateHomeworkRemarks(_, report._id)
      )
    }

    def tutorSelect = {
      val id = groupDetails.tutorId match {
          case null => ""
          case possibleId => possibleId.toString
        }
      SHtml.select(
        ("","") +: allTutors.sortWith(_.lastName < _.lastName).map(tutor => (tutor._id.toString, (tutor.firstName + " " + tutor.lastName))),
        Full(id),
        tutorId = _
      )
    }

    def locationSelect = {
      SHtml.selectObj(
        (Empty, "") +: allLocations.toList.map(location => (Full(location), location.toString)),
        Full(groupDetails.location:Box[Location]),
        (selectedLocation: Box[Location]) => location = selectedLocation
      )
    }

    def semesterSelect = {
      SHtml.selectObj(
        (Empty, "") +: allSemesters.toList.map(semester => (Full(semester), semester.toString)),
        Full(groupDetails.semester:Box[Semester]),
        (selectedSemester: Box[Semester]) => semester = selectedSemester
      )
    }

    def stationSubjectSelect = {
      SHtml.selectObj(
        (Empty, "") +: allStationSubjects.toList.map(subject => (Full(subject), subject.toString)),
        Full(groupDetails.stationSubject:Box[StationSubject]),
        (selectedSubject: Box[StationSubject]) => stationSubject = selectedSubject
      )
    }

    val groupDate = 
      if(possibleDetails.nonEmpty)
        new SimpleDateFormat("MM/dd/yyyy").format(groupDetails.date)
      else
        ""

    SHtml.makeFormsAjax andThen
    "#tutor" #> tutorSelect &
    "#station-number" #> SHtml.text(groupDetails.stationNumber.getOrElse(""), station =>  stationNumber = Some(station)) &
    "#location" #> locationSelect &
    "#semester" #> semesterSelect &
    "#station-subject" #> stationSubjectSelect &
    "#session-date" #> SHtml.text(groupDate, sessionDate = _) &
    "#assigned-homework" #> SHtml.textarea(groupDetails.homework.getOrElse(""), hw =>  homework = Some(hw)) &
    "#material-covered" #> SHtml.textarea(groupDetails.materialCovered.getOrElse(""), covered =>  materialCovered = Some(covered)) &
    ".students" #> SHtml.idMemoize { renderer =>
      ".student-entry" #> students.map(renderStudentReports(_)) &
      ".add-student" #> SHtml.ajaxOnSubmit(addStudent(renderer)) &
      "#save-report" #> SHtml.ajaxOnSubmit(createReport _)
    }
  }
}
