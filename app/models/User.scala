package models

import models.dao._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json
import scala.util.control.Exception.nonFatalCatch

case class User(sid: String)
object User {
  implicit val toJson = Json.writes[User]

  def all() = Users.all.map(a => User(a.sid))

  private[models] def get(id: Long) = DB.withSession { implicit session =>
    Query(Users).filter(_.id === id).firstOption.map(u => User(u.sid))
  }

  def get(sid: String) = DB.withSession { implicit session =>
    Query(Users).filter(_.sid === sid).firstOption.map(u => User(u.sid))
  }

  def roles(sid: String) = DB.withSession { implicit session =>
    val rdos = Query(Users).filter(_.sid === sid).flatMap(_.roles).list
    rdos.map(rdo => Role.get(rdo.id))
  }

  // Insert role, insert user-role
  /**
   * @return either Right(id) of the new role, or Left(errors).
   */
  def addRole(sid: String, role: Role) = DB.withSession { implicit session =>
    
    def insertUserRole(user : Long, role : Long) = {
      val either = nonFatalCatch.either(models.dao.UserRoles.insert((user, role)))
      either.left.map(a => Seq(a.getMessage()))      
    }
    
    for {
      user <- Query(Users).filter(_.sid === sid).firstOption.toRight(Seq("User " + sid + " does not exist in the system.")).right
      insertedRole <- models.Role.insert(role).right
      insertedUserRole <- insertUserRole(user.id, insertedRole).right
    } yield insertedRole
  }
}