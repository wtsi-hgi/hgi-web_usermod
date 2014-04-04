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