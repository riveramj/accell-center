package com.riveramj.util

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.common._
import net.liftweb.http.RedirectResponse

import com.riveramj.snippet._

object Paths {
  val index  = Menu.i("index")   / "index" >>
    EarlyResponse(() => Full(RedirectResponse(Tutors.menu.loc.calcDefaultHref)))

  def siteMap = SiteMap(
    index,
    Tutors.menu,
    Students.menu,
    SessionReports.menu
  )
}
