package models.dao

import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

trait StandardQueries[A] { self: play.api.db.slick.Config.driver.simple.Table[A] =>

  def all = DB.withSession { implicit session => 
    Query(this).list
  }
  def count = DB.withSession { implicit session =>
    Query(this.length).first
  }

  def insert(entity: A) = DB.withSession { implicit session =>
    (self : Table[A]).insert(entity)
  }
}