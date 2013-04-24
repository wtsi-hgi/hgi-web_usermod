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
  private def coaelesce(results : Seq[(RoleTypeDO, Option[String], Option[String])]) = results.groupBy(_._1).map { case (a,b) =>
    RoleType(a.name, a.description, b.collect{ case (_, Some(name), description) => ParameterType(name, description)})
  }

  def all() = DB.withSession { implicit session =>
    val q1 = for {
      ((rt, _), pt) <- RoleTypes.
      leftJoin(RoleTypeParameterTypes).on(_.id === _.rtId).
      leftJoin(ParameterTypes).on(_._2.ptId === _.id)
    } yield (rt, pt.name.?, pt.description.?)
    
    coaelesce(q1.list)
  }
  
  def get(name : String) = DB.withSession { implicit session =>
        val q1 = for {
      ((rt, _), pt) <- RoleTypes.
      leftJoin(RoleTypeParameterTypes).on(_.id === _.rtId).
      leftJoin(ParameterTypes).on(_._2.ptId === _.id) if (rt.name === name)
    } yield (rt, pt.name.?, pt.description.?)
    
    coaelesce(q1.list)
  }
  
  /**
   * Get all the parameters for a given role type.
   */
  def getParameters(name : String) = DB.withSession { implicit session =>
    val q1 = for {
      rt <- RoleTypes if rt.name === name
      pt <- rt.parameters
    } yield pt
    
    q1.list.map(a => ParameterType(a.name, a.description))
  }
  
  private def insert(rt : RoleType) = DB.withSession { implicit session =>
    val params = rt.parameters.map(ParameterType.getOrInsert)
    val roleType = RoleTypes.forInsert insert (rt.name, rt.description)
    params.foreach(RoleTypeParameterTypes.insert(roleType, _))
    roleType
  }
  
}