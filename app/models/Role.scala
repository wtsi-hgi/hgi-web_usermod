/*
Copyright (c) 2013, Wellcome Trust Sanger Institute

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of the Wellcome Trust Sanger Institute nor the 
      names of other contributors may be used to endorse or promote 
      products derived from this software without specific prior written 
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
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
   * Get the id for the specified role.
   */
  private[models] def find(role: Role) = DB.withSession { implicit session =>

    // Constructing a query for the parameters is annoying, so we get all roles and then filter
    val roleQ = for {
      ((r, p), pt) <- Roles leftJoin dao.Parameters on (_.id === _.roleId) leftJoin dao.ParameterTypes on (_._2.ptId === _.id)
      rt <- r.roleType if rt.name === role.name
    } yield (r.id, pt.name.?, p.value.?)

    val roles = roleQ.list.groupBy(_._1).map {
      case (a, b) =>
        (a, b.collect { case (_, Some(n), Some(v)) => Parameter(n, v) })
    }.filter(r => r._2 == role.parameters)

    // There should be precisely one role
    roles.map(_._1).headOption
  }

  /**
   * Get all users with the given role.
   */
  def users(role: Role) = DB.withSession { implicit session =>
    find(role) match {
      case Some(id) => UserRoles.filter(_.roleId === id).flatMap(_.user).list.map(u => User(u.sid, u.name))
      case None => Seq()
    }
  }

  /**
   * Insert a new role. This will fail if either the role type or any parameter type does not exist,
   * or if the parameters are not appropriate to that role. It will also fail if not all parameters have been provided.
   *
   * If the role already exists, just return the id of that.
   *
   * @Return either Right(id) of the new role, or Left(message) that describes the problem.
   */
  def add(role: Role): Either[Seq[String], Long] = DB.withSession { implicit session =>
    find(role) match {
      case Some(id) => Right(id)
      case None => {
        val q = RoleTypes.filter(_.name === role.name)
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
            val roleId = dao.Roles insert RoleDO(1,roleTypeId)
            parameters.foreach { case (id, value) => dao.Parameters insert ParameterDO(1, roleId, id, value) }
            roleId
          }
        }
      }
    }
  }

  /**
   * Remove a role. This will remove the role from all users.
   */
  def remove(role: Role) = DB.withSession { implicit session =>
    find(role) map { id =>
      UserRoles.filter(_.roleId === id).delete
      dao.Parameters.filter(_.roleId === id).delete
      Roles.filter(_.id === id).delete
    }
  }
}