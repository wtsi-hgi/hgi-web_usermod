package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.libs.json.JsError

import models._
import global.Authenticated.authenticated
import rules.WhoCanModify._
import rules.MetaRoles

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

  def delete(rolename: String) = authenticated { user =>
    Action {
      if (canSetGlobalRole(user)) {
        val roleType = models.RoleType.get(rolename)
        roleType match {
          case Some(rt) if !MetaRoles.all.contains(rt) => Ok(Json.obj("deleted" -> models.RoleType.delete(rt)))
          case Some(_) => Forbidden(Json.arr("Cannot remove metarole type: $rolename"))
          case None => NotFound(Json.arr(s"Cannot find role type: $rolename"))
        }
      } else {
        Forbidden(Json.arr("No permission to set global roles."))
      }
    }
  }

  def parameters(name: String) = Action {
    val parameters = models.RoleType.getParameters(name).map(a => Json.toJson(a))
    val json = Json.obj("parameters" -> parameters)

    Ok(json)
  }

}