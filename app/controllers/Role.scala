package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import models._
import play.api.libs.json.JsError
import scala.util.parsing.combinator.RegexParsers
import global.Authenticated.authenticated

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

  // TODO make this a verified action
  def add() = authenticated { user =>
    Action(parse.json) { request =>
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

  def delete(role: String) = authenticated { user =>
    Action {
      withParsedRole(role) { role =>
        models.Role.remove(role).
          map(a => Ok(Json.obj("removed" -> a))).
          getOrElse(BadRequest(s"Role: $role does not exist."))
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
        models.User.addRole(sid, role) match {
          case Right(id) => Ok(Json.obj("id" -> id))
          case Left(errs) => Forbidden(Json.arr(errs))
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
        val numRemoved = models.User.removeRole(sid, role)
        numRemoved match {
          case Some(number) => Ok(Json.obj("removed" -> number))
          case None => BadRequest(s"User $sid does not possess role $role")
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