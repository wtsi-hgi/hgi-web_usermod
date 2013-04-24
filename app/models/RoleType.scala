package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import models.dao._

/**
 * Actual class presented to the rest of the application.
 */
case class RoleType(id: Long, name: String, description: Option[String], parameters: Seq[ParameterType])
object RoleType {
  
  implicit val toJson = Json.writes[RoleType]

  def all() = DB.withSession { implicit session =>
    val q1 = for {
      rt <- RoleTypes
      pt <- rt.parameters
    } yield (rt, pt)
    
    q1.list.groupBy(_._1).map { case (a,b) =>
      RoleType(a.id, a.name, a.description, b.map(_._2))
    }
  }
  
  def count = RoleTypes.count
}