package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import models.User
import models.RoleType

object API extends Controller {

  def allUsers() = Action {
    val users = User.all().map(a => Json.toJson(a))
    val json = Json.obj("users" -> users)
    
    Ok(Json.prettyPrint(json))
  }
  
  def allRoles() = Action {
    val roles = RoleType.all().map(a => Json.toJson(a))
    val json = Json.obj("roles" -> roles)
    
    Ok(Json.prettyPrint(json))
  }

}