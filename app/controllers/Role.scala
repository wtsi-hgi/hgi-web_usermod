package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import models._
import play.api.libs.json.JsError

object Role extends Controller {

  def all() = Action {
    val roles = models.Role.all().map(a => Json.toJson(a))
    val json = Json.obj("roles" -> roles)

    Ok(json)
  }

  // TODO make this a verified action
  def addRole() = Action(parse.json) { request =>
    request.body.validate[Role].map { role =>
      models.Role.insert(role) match {
        case Right(id) => Ok(Json.obj("id" -> id))
        case Left(errs) => Forbidden(Json.arr(errs))
      }
    }.recoverTotal { jsErr =>
      BadRequest(JsError.toFlatJson(jsErr))
    }
  }

}