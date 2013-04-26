package models

import models.dao._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json

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
}