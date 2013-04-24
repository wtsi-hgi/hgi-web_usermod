package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json

import models._

object RoleType extends Controller {

  def all() = Action {
    val roles = models.RoleType.all().map(a => Json.toJson(a))
    val json = Json.obj("roles" -> roles)

    Ok(Json.prettyPrint(json))
  }

  def get(name: String) = Action {
    val roles = models.RoleType.get(name).map(a => Json.toJson(a)).headOption

    Ok(roles.map(Json.prettyPrint).getOrElse(""))
  }
  
  def parameters(name : String) = Action {
    val parameters = models.RoleType.getParameters(name).map(a => Json.toJson(a))
    val json = Json.obj("parameters" -> parameters)
    
    Ok(Json.prettyPrint(json))
  }

}