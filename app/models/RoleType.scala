package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

/**
 * A parameter to a role type.
 */
case class ParameterType(id: Long, name: String, description: Option[String])
object ParameterType {
  implicit val writes = Json.writes[ParameterType]

  // Queries
  def all() = DB.withSession { implicit session =>
    Query(ParameterTypes).list
  }
}
object ParameterTypes extends Table[ParameterType]("PARAMETER_TYPES") with StandardQueries[ParameterType]{
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def description = column[String]("description", O.Nullable)

  def autoInc = * returning id
  def * = id ~ name ~ description.? <> (ParameterType.apply _, ParameterType.unapply _)
}

/**
 * Role type is the prototype for a role.
 */
case class RoleType(id: Long, name: String, description: Option[String])
object RoleType {
  implicit val writes = Json.writes[RoleType]

  // Queries
  def all() = DB.withSession { implicit session =>
    Query(RoleTypes).list
  }
}

object RoleTypes extends Table[RoleType]("ROLE_TYPES")  with StandardQueries[RoleType] {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def description = column[String]("description", O.Nullable)

  def autoInc = * returning id
  def * = id ~ name ~ description.? <> (RoleType.apply _, RoleType.unapply _)
}

object RoleTypeParameterTypes extends Table[(Long, Long)]("ROLE_TYPE_PARAMETER_TYPE") with StandardQueries[(Long, Long)]{
  def rtId = column[Long]("role_type_id", O.NotNull)
  def ptId = column[Long]("parameter_type_id", O.NotNull)
  def * = rtId ~ ptId

  def roleType = foreignKey("role_type_fk", rtId, RoleTypes)(_.id)
  def parameterType = foreignKey("parameter_type_fk", ptId, ParameterTypes)(_.id)
}