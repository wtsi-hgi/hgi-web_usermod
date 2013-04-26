package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json

import models._

object API extends Controller {

  def allUsers() = Action {
    val users = User.all().map(a => Json.toJson(a))
    val json = Json.obj("users" -> users)
    
    Ok(json)
  }
  
  def getUser(sid : String) = Action {
    val user = User.get(sid).map(a => Json.toJson(a))
    user.map(Ok(_)).getOrElse(NotFound("No user with sid "+sid+" is registered in the system."))        
  }
  
  def userRoles(sid : String) = Action {
    val roles = User.roles(sid).map(a => Json.toJson(a))
    val json = Json.obj("roles" -> roles)
    
    Ok(json)
  }
}