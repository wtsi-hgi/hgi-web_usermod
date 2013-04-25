package models.dao

import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

/**
 * Role type is the prototype for a role.
 */
private[models] case class RoleTypeDO(id: Long, name: String, description: Option[String])

private[models] object RoleTypes extends Table[RoleTypeDO]("ROLE_TYPES")  with StandardQueries[RoleTypeDO] {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def description = column[String]("description", O.Nullable)

  def * = id ~ name ~ description.? <> (RoleTypeDO.apply _, RoleTypeDO.unapply _)
  def forInsert = name ~ description.? returning id
  
  def nameIdx = index("idx_name", name, unique = false)
  
  def parameters = RoleTypeParameterTypes.filter(_.rtId === id).flatMap(_.parameterType)
}

private[models] object RoleTypeParameterTypes extends Table[(Long, Long)]("ROLE_TYPE_PARAMETER_TYPE") with StandardQueries[(Long, Long)]{
  def rtId = column[Long]("role_type_id", O.NotNull)
  def ptId = column[Long]("parameter_type_id", O.NotNull)
  def * = rtId ~ ptId

  def roleType = foreignKey("rtpt_role_type_fk", rtId, RoleTypes)(_.id)
  def parameterType = foreignKey("rtpt_parameter_type_fk", ptId, ParameterTypes)(_.id)
}