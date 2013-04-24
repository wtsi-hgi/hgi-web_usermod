package models.dao

import play.api.db.slick.Config.driver.simple._

private[models] case class ParameterDO(id : Long, ptId : Long, value : String)
class Parameters extends Table[ParameterDO]("PARAMETERS") {
  def id = column[Long]("id", O.PrimaryKey)
  def ptId = column[Long]("parameter_type_id", O.NotNull)
  def roleId = column[Long]("role_id", O.NotNull)
  def value = column[String]("value", O.NotNull)
  
  def * = id ~ ptId ~ value <> (ParameterDO.apply _, ParameterDO.unapply _)
  
  def role = foreignKey("p_role_fk", roleId, Roles)(_.id)
  def parameterType = foreignKey("p_parameter_type_fk", ptId, ParameterTypes)(_.id)
}