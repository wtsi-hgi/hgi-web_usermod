package models.dao

import play.api.libs.functional.syntax._
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * A parameter to a role type.
 */
private[models] case class ParameterTypeDO(id: Long, name: String, description: Option[String])
object ParameterTypeDO {
  implicit val toJson = Json.writes[ParameterTypeDO]
}

class ParameterTypes extends Table[ParameterTypeDO]("PARAMETER_TYPES") with StandardQueries[ParameterTypeDO] {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def description = column[String]("description", O.Nullable)

  def autoInc = * returning id
  def * = id ~ name ~ description.? <> (ParameterTypeDO.apply _, ParameterTypeDO.unapply _)
}