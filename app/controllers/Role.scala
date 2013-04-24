package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json

import models._

object Role extends Controller {
  
  def all() = Action {
    val roles = models.Role.all().map(a => Json.toJson(a))
    val json = Json.obj("roles" -> roles)
    
    Ok(Json.prettyPrint(json))
  }

}