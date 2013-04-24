package models.dao

import play.api.db.slick.Config.driver.simple._

/**
 * A parameter to a role type.
 */
private[models] case class ParameterTypeDO(id: Long, name: String, description: Option[String])

class ParameterTypes extends Table[ParameterTypeDO]("PARAMETER_TYPES") with StandardQueries[ParameterTypeDO] {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def description = column[String]("description", O.Nullable)

  def * = id ~ name ~ description.? <> (ParameterTypeDO.apply _, ParameterTypeDO.unapply _)
  def forInsert = name ~ description.? returning id
}