package models

import play.api.Play.current
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.db.slick.Config.driver.simple._

import dao._

case class Role(name: String, parameters: Seq[Parameter])
object Role {
  
  implicit val toJson = Json.writes[Role]

  private def coaelesce(r: Seq[(String, Option[String], Option[String])]) = {
    r.groupBy(_._1).map { case (a, b) => Role(a, b.collect { case (_, Some(n), Some(v)) => Parameter(n, v) }) }
  }

  def all() = DB.withSession { implicit session =>
    val q1 = for {
      (r, p) <- Roles leftJoin dao.Parameters on (_.id === _.roleId)
      pt <- p.parameterType
      rt <- r.roleType
    } yield (rt.name, pt.name.?, p.value.?)

    coaelesce(q1.list)
  }
}