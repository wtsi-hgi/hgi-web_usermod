package controllers

import play.api._
import play.api.mvc._
import global.Authenticated.authenticated

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def testAuth = authenticated { user =>
    Action {
      Ok(s"You have authenticated successfully, $user.")
    }
  }

}