package models.dao

import play.api.db.slick.Config.driver.simple._

case class RoleDO(id : Long, rtId : Long)
class Roles extends Table[RoleDO]("ROLES") {
  def id = column[Long]("id", O.PrimaryKey)
  def rtId = column[Long]("role_type_id", O.NotNull)
  
  def * = id ~ rtId <> (RoleDO.apply _, RoleDO.unapply _)
  
  def roleType = foreignKey("role_type_fk", rtId, RoleTypes)(_.id)
}