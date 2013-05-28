package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.libs.json.JsError

import models._
import global.Authenticated.authenticated
import rules.WhoCanModify._

import models._

object RoleType extends Controller {

  def all() = Action {
    val roles = models.RoleType.all().map(a => Json.toJson(a))
    val json = Json.obj("roles" -> roles)

    Ok(json)
  }

  def get(name: String) = Action {
    val roles = models.RoleType.get(name).map(a => Json.toJson(a)).headOption

    Ok(roles.getOrElse(Json.obj()))
  }

  def add() = authenticated { user =>
    Action(parse.json) { request =>
      request.body.validate[RoleType].map { role =>
        if (canSetGlobalRole(user)) {
          models.RoleType.add(role) match {
            case Right(id) => Ok(Json.obj("id" -> id))
            case Left(errs) => Forbidden(Json.arr(errs))
          }
        } else {
          Forbidden(Json.arr("No permission to set global roles."))
        }
      }.recoverTotal { jsErr =>
        BadRequest(JsError.toFlatJson(jsErr))
      }
    }
  }

  def parameters(name: String) = Action {
    val parameters = models.RoleType.getParameters(name).map(a => Json.toJson(a))
    val json = Json.obj("parameters" -> parameters)

    Ok(json)
  }

}