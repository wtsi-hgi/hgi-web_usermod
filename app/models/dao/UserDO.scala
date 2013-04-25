package models.dao

import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

private[models] case class UserDO(id: Long, sid: String)

private[models] object Users extends Table[UserDO]("USERS") with StandardQueries[UserDO] {
  
  def id = column[Long]("id", O.PrimaryKey)
  def sid = column[String]("sid", O.NotNull)

  def * = id ~ sid <> (UserDO.apply _, UserDO.unapply _)
}

private[models] object UserRoles extends Table[(Long, Long)]("USER_ROLES") with StandardQueries[(Long, Long)] {
  def userId = column[Long]("user_id", O.NotNull)
  def roleId = column[Long]("role_id", O.NotNull)
  
  def * = userId ~ roleId
  
  def userFk = foreignKey("ur_user_fk", userId, Users)(_.id)
  def roleFk = foreignKey("ur_role_fk", roleId, Roles)(_.id)
}