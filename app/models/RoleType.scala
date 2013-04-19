package models
import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

/**
 * A parameter to a role type.
 */
case class ParameterType(name: String, description: String)
object ParameterType {
  implicit val writes = Json.writes[ParameterType]
}

/**
 * Role type is the prototype for a role.
 */
case class RoleType(name: String, description: String, parameters: Seq[ParameterType])
object RoleType {
  implicit val writes = Json.writes[RoleType]

  def all() = DB.withConnection { implicit c =>
    val roles = SQL(
      """
      select r.name, r.description, p.name, p.description
      from role_type r
      left join role_type_parameter_type rp on rp.role_type_id = r.id
      left join parameter_type p on rp.parameter_type_id = p.id
    """).as(str("role_type.name") ~ 
        get[Option[String]]("role_type.description") ~ 
        get[Option[String]]("parameter_type.name") ~ 
        get[Option[String]]("parameter_type.description") map flatten *)
    
    roles.groupBy(a => (a._1, a._2)).map {case (a,b) =>
      val params = b.collect { case (_, _, Some(c), d) => ParameterType(c, d.getOrElse("")) }
      RoleType(a._1, a._2.getOrElse(""), params)
    }
  }
}