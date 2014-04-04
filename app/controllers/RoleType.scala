/*
Copyright (c) 2013, Wellcome Trust Sanger Institute

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of the Wellcome Trust Sanger Institute nor the 
      names of other contributors may be used to endorse or promote 
      products derived from this software without specific prior written 
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
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
          case Some(rt) if !MetaRoles.all.contains(rt) => {
            Logger.debug(rt.toString())
            Logger.debug(MetaRoles.delegate.toString)
            Logger.debug("Metaroles contains? " + MetaRoles.all.contains(rt))
            Ok(Json.obj("deleted" -> models.RoleType.delete(rt)))
          }
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