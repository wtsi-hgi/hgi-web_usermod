package models.dao

import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

private[models] case class UserDO(id: Long, sid: String)

private[models] class Users extends Table[UserDO]("USERS") with StandardQueries[UserDO] {
  
  def id = column[Long]("id", O.PrimaryKey)
  def sid = column[String]("sid", O.NotNull)

  def * = id ~ sid <> (UserDO.apply _, UserDO.unapply _)
}