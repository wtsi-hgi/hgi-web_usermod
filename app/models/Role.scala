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
  implicit val fromJson = Json.reads[Role]

  private def coaelesce(r: Seq[(String, Option[String], Option[String])]) = {
    r.groupBy(_._1).map { case (a, b) => Role(a, b.collect { case (_, Some(n), Some(v)) => Parameter(n, v) }) }
  }

  def all() = DB.withSession { implicit session =>
    val q1 = for {
      ((r, p), pt) <- Roles leftJoin dao.Parameters on (_.id === _.roleId) leftJoin dao.ParameterTypes on (_._2.ptId === _.id)
      rt <- r.roleType
    } yield (rt.name, pt.name.?, p.value.?)

    coaelesce(q1.list)
  }

  private[models] def get(id: Long) = DB.withSession { implicit session =>
    val q1 = for {
      ((r, p), pt) <- Roles leftJoin dao.Parameters on (_.id === _.roleId) leftJoin dao.ParameterTypes on (_._2.ptId === _.id) if r.id === id
      rt <- r.roleType
    } yield (rt.name, pt.name.?, p.value.?)

    coaelesce(q1.list).headOption
  }

  /**
   * Insert a new role. This will fail if either the role type or any parameter type does not exist,
   * or if the parameters are not appropriate to that role. It will also fail if not all parameters have been provided.
   *
   * @Return either Right(id) of the new role, or Left(message) that describes the problem.
   */
  def insert(role: Role): Either[Seq[String], Long] = DB.withSession { implicit session =>
    val q = Query(RoleTypes).filter(_.name === role.name)
    val roleType = q.firstOption.toRight("Cannot add role for missing role type: " + role.name)
    val parameterIdMap = q.flatMap(_.parameters).map(a => a.name -> a.id).list.toMap

    val providedParameters = role.parameters.map(_.name).toSet
    val missingParameters = (parameterIdMap.keySet -- providedParameters)
    val surplusParameters = providedParameters -- parameterIdMap.keySet

    val ids = (roleType, missingParameters, surplusParameters) match {
      case (Right(roleType), m, s) if (m.isEmpty && s.isEmpty) => Right(
        (roleType.id, role.parameters map (p => parameterIdMap(p.name) -> p.value)))
      case (r, m, s) => Left(
        r.left.toSeq ++:
          m.toSeq.map("Missing parameter value for parameter type: " + _) ++:
          s.toSeq.map("Cannot add parameter for missing parameter type: " + _))
    }

    ids.right.map {
      case (roleTypeId, parameters) => {
        val roleId = dao.Roles.forInsert insert roleTypeId
        parameters.foreach { case (id, value) => dao.Parameters.forInsert insert (roleId, id, value) }
        roleId
      }
    }
  }
}