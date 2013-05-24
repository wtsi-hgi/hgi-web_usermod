package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import global.Authenticated.authenticated
import global.LDAPProvider

object Application extends Controller {
    val ldap = current.configuration.getString("ldap.server").map(new LDAPProvider(_))

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def testAuth = authenticated { user =>
    Action {
      Ok(s"You have authenticated successfully, $user.")
    }
  }
  
  def searchUsers(user : String) = Action {
    ldap match {
      case Some(ld) => Ok(ld.search(user).mkString("\n"))
      case None => InternalServerError("Missing ldap.server value in config file.")
    }
  }

}