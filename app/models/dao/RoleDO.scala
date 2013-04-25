package models.dao

import play.api.db.slick.Config.driver.simple._

case class RoleDO(id : Long, rtId : Long)
private[models] class Roles extends Table[RoleDO]("ROLES") with StandardQueries[RoleDO] {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def rtId = column[Long]("role_type_id", O.NotNull)
  
  def * = id ~ rtId <> (RoleDO.apply _, RoleDO.unapply _)
  def forInsert = rtId returning id
  
  def roleType = foreignKey("r_role_type_fk", rtId, RoleTypes)(_.id)
  def parameters = Parameters.filter(_.roleId == id)
}