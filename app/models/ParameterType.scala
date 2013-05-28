package models

import play.api.Play.current
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.db.slick.Config.driver.simple._

import dao.ParameterTypes

case class ParameterType(name : String, description : Option[String]) {
  def instantiate(value : String) = Parameter(name, value)
}

object ParameterType { 
  implicit val toJson = Json.writes[ParameterType]
  implicit val fromJson = Json.reads[ParameterType]
  
  def get(name : String) = DB.withSession { implicit session =>
    Query(ParameterTypes).filter(_.name === name).firstOption
  }
  
  def add(pt : ParameterType) = getOrInsert(pt)
  
  /**
   * Check whether a matching parameter type already exists in the database, and
   * return its id if so. Otherwise, add a new parameter type and return that id.
   */
  private[models] def getOrInsert(pt : ParameterType) : Long = DB.withSession { implicit session =>
    get(pt.name).map(_.id).getOrElse(insert(pt))
  }
  
  private def insert(pt : ParameterType) : Long = DB.withSession { implicit session =>
    ParameterTypes.forInsert insert (pt.name, pt.description)
  }
}