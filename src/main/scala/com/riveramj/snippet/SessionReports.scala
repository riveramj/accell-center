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
import java.text.SimpleDateFormat
import scala.xml.NodeSeq

import com.riveramj.model._

object SessionReports {
  val menu = Menu.i("Session Reports") / "session-reports"
}

class SessionReports extends Loggable {
  def list = {
    val reports = SessionReport.findAll

    def deleteReport(reportId: ObjectId): JsCmd = {
      SessionReport.find(reportId).map(_.delete)

      SessionReport.find(reportId) match {
        case None =>
          JsCmds.Run("$('#" + reportId + "').parent().parent().prev().remove()") &
          JsCmds.Run("$('#" + reportId + "').parent().parent().remove()")
        case _ => 
          logger.error(s"couldn't delete student with id $reportId")
      }
    }

    def renderSessionReport(rows: NodeSeq, report: SessionReport) = {
      val student = Student.find(report.studentId)
      val groupDetails = SessionReportGroupDetails.find(report.sessionReportGroupDetailsId)
      val tutor = groupDetails.flatMap(groupEntry => Tutor.find(groupEntry.tutorId))

      {
        ".studentName *" #> student.map(s => s.firstName + " " + s.lastName) &
        ".tutorName *" #> tutor.map(t => t.firstName + " " + t.lastName) &
        ".subject *" #> groupDetails.flatMap(_.stationSubject).map(_.toString) &
        ".semester *" #> groupDetails.flatMap(gd => gd.semester.map(_.toString)) &
        ".date *" #> groupDetails.map(groupDetail => new SimpleDateFormat("MM/dd/yyyy").format(groupDetail.date)) &
        ".progression *" #> report.progression.map(_.toString) &
        ".session-report-details [id]" #> report._id.toString &
        ".summary [data-target]" #> ("#" + report._id.toString) &
        ".delete-report [onclick]" #> SHtml.ajaxInvoke(() => {
          JsCmds.Confirm("Are you sure you want to delete the report?", {
            SHtml.ajaxInvoke(() => {
              deleteReport(report._id)
            }).cmd
          })
        })
      } apply rows
    }

    ClearClearable andThen
    "tbody *" #> { rows: NodeSeq =>
      val summaryRow = (".summary ^^" #> "unused") apply rows 
      val detailsRow = (".details ^^" #> "unused") apply rows

      reports.map { report =>
        renderSessionReport(summaryRow ++ detailsRow, report)
      }.flatten
    }
  }
}
