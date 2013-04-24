package models

import play.api.Play.current
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.db.slick.Config.driver.simple._

import dao.ParameterTypes

case class ParameterType(name : String, description : Option[String])
object ParameterType { 
  implicit val toJson = Json.writes[ParameterType]
  implicit val fromJson = Json.reads[ParameterType]
  
  /**
   * Check whether a matching parameter type already exists in the database, and
   * return its id if so. Otherwise, add a new parameter type and return that id.
   */
  def getOrInsert(pt : ParameterType) : Long = DB.withSession { implicit session =>
    val existing = Query(ParameterTypes).filter(_.name === pt.name).map(_.id).firstOption
    existing.getOrElse(insert(pt))
  }
  
  private def insert(pt : ParameterType) : Long = DB.withSession { implicit session =>
    ParameterTypes.forInsert insert (pt.name, pt.description)
  }
}