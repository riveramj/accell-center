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

object SessionReports {
  val menu = Menu.i("Session Reports") / "session-reports"
}

class SessionReports extends Loggable {

}
