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
case class RoleType(name: String, description: Option[String], parameters: Seq[ParameterType])
object RoleType {
  
  implicit val toJson = Json.writes[RoleType]
  implicit val fromJson = Json.reads[RoleType]
  
  /**
   * Coaelesce pairs of rtdo/ptdo into a single RoleType with associated ParameterTypes.
   */
  private def coaelesce(results : Seq[(RoleTypeDO, ParameterTypeDO)]) = results.groupBy(_._1).map { case (a,b) =>
    RoleType(a.name, a.description, b.map{ case (_, c) => ParameterType(c.name, c.description)})
  }

  def all() = DB.withSession { implicit session =>
    val q1 = for {
      rt <- RoleTypes
      pt <- rt.parameters
    } yield (rt, pt)
    
    coaelesce(q1.list)
  }
  
  def byName(name : String) = DB.withSession { implicit session =>
    val q1 = for {
      rt <- RoleTypes if rt.name === name
      pt <- rt.parameters
    } yield (rt, pt)
    
    coaelesce(q1.list)
  }
  
  def insert(rt : RoleType) = DB.withSession { implicit session =>
    val params = rt.parameters.map(ParameterType.getOrInsert)
    val roleType = RoleTypes.forInsert insert (rt.name, rt.description)
    params.foreach(RoleTypeParameterTypes.insert(roleType, _))
    roleType
  }
  
}