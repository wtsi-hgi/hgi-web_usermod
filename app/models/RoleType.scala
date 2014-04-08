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

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import models.dao._

import scalaz._
import Scalaz._

/**
 * Actual class presented to the rest of the application.
 */
case class RoleType(name: String, description: Option[String], parameters: Seq[ParameterType]) {
  def instantiate(pvalues: Map[String, String]) = {
    parameters.map(a => pvalues.get(a.name).map(a.instantiate)).toList.sequence.map(a => Role(name, a))
  }
  def instantiate(pvalues: Seq[Parameter]) = {
    if (pvalues.map(_.name) == parameters.map(_.name)) Some(Role(name, pvalues)) else None
  }
}

object RoleType {

  implicit val toJson = Json.writes[RoleType]
  implicit val fromJson = Json.reads[RoleType]

  /**
   * Coaelesce pairs of rtdo/ptdo into a single RoleType with associated ParameterTypes.
   */
  private def coaelesce(results: Seq[(RoleTypeDO, Option[String], Option[String])]) = results.groupBy(_._1).map {
    case (a, b) =>
      RoleType(a.name, a.description, b.collect { case (_, Some(name), description) => ParameterType(name, description) })
  }

  def all() = DB.withSession { implicit session =>
    val q1 = for {
      ((rt, _), pt) <- RoleTypes.
        leftJoin(RoleTypeParameterTypes).on(_.id === _.rtId).
        leftJoin(ParameterTypes).on(_._2.ptId === _.id)
    } yield (rt, pt.name.?, pt.description.?)

    coaelesce(q1.list)
  }

  def get(name: String) = DB.withSession { implicit session =>
    val q1 = for {
      ((rt, _), pt) <- RoleTypes.
        leftJoin(RoleTypeParameterTypes).on(_.id === _.rtId).
        leftJoin(ParameterTypes).on(_._2.ptId === _.id) if (rt.name === name)
    } yield (rt, pt.name.?, pt.description.?)

    coaelesce(q1.list).headOption
  }

  def add(role: RoleType) = DB.withSession { implicit session =>
    RoleTypes.filter(_.name === role.name).firstOption match {
      case Some(rt) if (get(rt.name).map(_ == role).getOrElse(false)) => Right(rt.id)
      case None => Right(insert(role))
      case _ => Left(Seq("Role already exists but does not match!"))
    }
  }

  def delete(role: RoleType) = DB.withSession { implicit session =>
    // Need to kill all parameters + roles using this role type.
    val roleType = RoleTypes.filter(_.name === role.name)
    val roles = roleType flatMap (a => Roles.filter(_.rtId === a.id));
    {
      val ids = roleType.map(_.id).list
      for (id <- ids) {
        RoleTypeParameterTypes.filter(_.rtId === id).delete
      }
    }
    {
      val ids = roles.map(_.id).list
      for (id <- ids) {
        dao.UserRoles.filter(_.roleId === id).delete
        dao.Parameters.filter(_.roleId === id).delete
        dao.Roles.filter(_.id === id).delete
      }
    }
    roleType.delete
  }

  def addParameter(name: String, parameter: ParameterType) = DB.withSession { implicit session =>
    val pId = models.ParameterType.add(parameter)
    val roleId = RoleTypes.filter(_.name === name).map(_.id).firstOption

    roleId match {
      case Some(id) => Right(RoleTypeParameterTypes.insert(id, pId))
      case None => Left(Seq(s"Cannot add parameter to non-existent role: $name"))
    }
  }

  /**
   * Get all the parameters for a given role type.
   */
  def getParameters(name: String) = DB.withSession { implicit session =>
    val q1 = for {
      rt <- RoleTypes if rt.name === name
      pt <- rt.parameters
    } yield pt

    q1.list.map(a => ParameterType(a.name, a.description))
  }

  private[models] def insert(rt: RoleType) = DB.withSession { implicit session =>
    val params = rt.parameters.map(ParameterType.getOrInsert)
    val roleType = (RoleTypes returning RoleTypes.map(_.id)) insert RoleTypeDO(1, rt.name, rt.description)
    params.foreach(RoleTypeParameterTypes.insert(roleType, _))
    roleType
  }

}