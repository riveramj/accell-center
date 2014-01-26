package com.riveramj.util

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.common._
import net.liftweb.http._

import com.riveramj.snippet._

object Paths {
  val index  = Menu.i("index")   / "index" >>
    EarlyResponse(() => Full(RedirectResponse(Tutors.menu.loc.calcDefaultHref)))

  def siteMap = SiteMap(
    index,
    EditTutor.menu,
    Tutors.menu,
    EditStudent.menu,
    Students.menu,
    CreateSessionReports.menu,
    SessionReports.menu
  )

  def MenuId(name: String) = {
    Menu.param[String](name, S ? name,
      Full(_),
      id => id
    )
  }
}
