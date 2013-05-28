package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import models._
import play.api.libs.json.JsError
import global.Authenticated.authenticated
import rules.WhoCanModify.canDo

object User extends Controller {

  def all() = Action {
    val users = models.User.all().map(a => Json.toJson(a))
    val json = Json.obj("users" -> users)

    Ok(json)
  }

  def get(sid: String) = Action {
    val user = models.User.get(sid).map(a => Json.toJson(a))
    user.map(Ok(_)).getOrElse(NotFound("No user with sid " + sid + " is registered in the system."))
  }

  def roles(sid: String) = Action {
    val roles = models.User.roles(sid).map(a => Json.toJson(a))
    val json = Json.obj("roles" -> roles)

    Ok(json)
  }

  def addRole(sid: String) = authenticated { user =>
    Action(parse.json) { request =>
      request.body.validate[Role].map { role =>
        if (canDo(user, sid, role)) {
          models.User.addRole(sid, role) match {
            case Right(id) => Ok(Json.obj("id" -> id))
            case Left(errs) => Forbidden(Json.arr(errs))
          }
        } else {
          Forbidden(Json.arr("No permission to add role."))
        }
      }.recoverTotal { jsErr =>
        BadRequest(JsError.toFlatJson(jsErr))
      }
    }
  }
}