package com.riveramj.util

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.common._
import net.liftweb.http.RedirectResponse

object Paths {
  // Roots.
  val index  = Menu.i("index")   / "index" 
  val tutors  = Menu.i("tutors")   / "tutors" 

  def siteMap = SiteMap(
    index,
    tutors
  )
}
