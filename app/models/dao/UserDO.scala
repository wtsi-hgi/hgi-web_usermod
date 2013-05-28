package models.dao

import java.sql.Date
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

private[models] case class UserDO(id: Long, sid: String, name : String)

private[models] class Users extends Table[UserDO]("USERS") with StandardQueries[UserDO] {
  
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def sid = column[String]("sid", O.NotNull)
  def name = column[String]("name")
  
  def * = id ~ sid ~ name <> (UserDO.apply _, UserDO.unapply _)
  def forInsert = sid ~ name returning id
  
  def roles = UserRoles.filter(_.userId === id).flatMap(_.role)
}

private[models] class UserRoles extends Table[(Long, Long)]("USER_ROLES") with StandardQueries[(Long, Long)] {
  def userId = column[Long]("user_id", O.NotNull)
  def roleId = column[Long]("role_id", O.NotNull)
  
  def * = userId ~ roleId
  
  def user = foreignKey("ur_user_fk", userId, Users)(_.id)
  def role = foreignKey("ur_role_fk", roleId, Roles)(_.id)
}