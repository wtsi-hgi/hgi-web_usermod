package models

import play.api.db.slick.Config.driver.simple._

case class Parameter(id : Long, ptId : Long, value : String)
object Parameters extends Table[Parameter]("PARAMETERS") {
  def id = column[Long]("id", O.PrimaryKey)
  def ptId = column[Long]("parameter_type_id", O.NotNull)
  def value = column[String]("value", O.NotNull)
  
  def * = id ~ ptId ~ value <> (Parameter.apply _, Parameter.unapply _)
  
  def parameterType = foreignKey("parameter_type_fk", ptId, ParameterTypes)(_.id)
}

case class Role(id : Long, rtId : Long)
object Roles extends Table[Role]("ROLES") {
  def id = column[Long]("id", O.PrimaryKey)
  def rtId = column[Long]("role_type_id", O.NotNull)
  
  def * = id ~ rtId <> (Role.apply _, Role.unapply _)
  
  def roleType = foreignKey("role_type_fk", rtId, RoleTypes)(_.id)
}