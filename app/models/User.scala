package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.dao.Users

case class User(id: Long, sid: String)
object User {
  def all() = Users.all.map(a => User(a.id, a.sid))
  implicit val toJson = Json.writes[User]
}