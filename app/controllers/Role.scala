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
import scala.util.parsing.combinator.RegexParsers
import global.Authenticated.authenticated
import rules.WhoCanModify._

object Role extends Controller {

  def all() = Action {
    val roles = Json.toJson(models.Role.all())
    val json = Json.obj("roles" -> roles)

    Ok(json)
  }

  private[this] def withParsedRole[A <: Result](role: String)(f: Role => A) = {
    URIParameters.parse(role) match {
      case URIParameters.Success((name, keyvals), _) => {
        val role = models.Role(name, keyvals map (Function.tupled(Parameter.apply)))
        f(role)
      }
      case URIParameters.Failure(msg, _) => BadRequest(s"Parse failure: $msg \n Input: $role")
      case URIParameters.Error(msg, _) => BadRequest(s"Parse error: $msg \n Input: $role")
    }
  }

  def add() = authenticated { user =>
    Action(parse.json) { request =>
      request.body.validate[Role].map { role =>
        if (canSetGlobalRole(user)) {
          models.Role.add(role) match {
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

  def delete(role: String) = authenticated { user =>
    Action {
      withParsedRole(role) { role =>
        if (canSetGlobalRole(user)) {
          models.Role.remove(role).
            map(a => Ok(Json.obj("removed" -> a))).
            getOrElse(BadRequest(s"Role: $role does not exist."))

        } else {
          Forbidden("No permission to set global roles.")
        }
      }
    }
  }

  /**
   * Look up a specific role.
   *
   * @param role The role parameter will be encoded in the form rolename;(paramname=paramvalue,)*
   */
  def users(role: String) = Action {
    withParsedRole(role) { role =>
      val users = models.Role.users(role)
      Ok(Json.toJson(users))
    }
  }

  def addUserRole(sid: String, role: String) = authenticated { user =>
    Action {
      withParsedRole(role) { role =>
        if (canDo(user, sid, role)) {
          models.User.addRole(sid, role) match {
            case Right(id) => Ok(Json.obj("id" -> id))
            case Left(errs) => Forbidden(Json.arr(errs))
          }
        } else {
          Forbidden("No permission to add role.")
        }
      }
    }
  }

  def hasUserRole(sid: String, role: String) = Action {
    withParsedRole(role) { role =>
      val hasRole = models.User.hasRole(sid, role)
      Ok(Json.obj("has_role" -> hasRole))
    }
  }

  def deleteUserRole(sid: String, role: String) = authenticated { user =>
    Action {
      withParsedRole(role) { role =>
        if (canDo(user, sid, role)) {
          val numRemoved = models.User.removeRole(sid, role)
          numRemoved match {
            case Some(number) => Ok(Json.obj("removed" -> number))
            case None => BadRequest(s"User $sid does not possess role $role")
          }
        } else {
          Forbidden("No permission to remove role.")
        }
      }
    }
  }

  /**
   * Parser for path segments using foo;bar=baz syntax
   */
  object URIParameters extends RegexParsers {
    override type Elem = Char
    def part = """[a-zA-Z0-9_-]*""".r
    def keyval = part ~ "=" ~ part ^^ { case k ~ _ ~ v => (k, v) }
    def keyvals = repsep(keyval, ",")
    def whole = part ~ opt("[;&]".r ~> keyvals) ^^ { case n ~ v => (n, v.getOrElse(Seq())) }

    def parse(s: String) = parseAll(whole, s)
  }

}