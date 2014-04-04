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

import models.dao._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json
import scala.util.control.Exception.nonFatalCatch
import javax.naming.directory.InitialDirContext
import global.LDAPProvider

case class User(sid: String, name : String)
object User {
  implicit val toJson = Json.writes[User]
  val ldap = current.configuration.getString("ldap.server").map(new LDAPProvider(_))

  def all() = Users.all.map(a => User(a.sid, a.name))

  private def add(user: User) = DB.withSession { implicit session =>
    Users.forInsert insert (user.sid, user.name)
  }

  private[models] def get(id: Long) = DB.withSession { implicit session =>
    Query(Users).filter(_.id === id).firstOption.map(u => User(u.sid, u.name))
  }

  def get(sid: String) = DB.withSession { implicit session =>
    Query(Users).filter(_.sid === sid).firstOption.map(u => User(u.sid, u.name))
  }

  /**
   * Get a user from the database, or else fetch from LDAP and add to the database.
   */
  private def getOrFetchLdap(sid: String): Option[UserDO] = DB.withSession { implicit session =>
    (Query(Users).filter(_.sid === sid).firstOption, ldap) match {
      case (u @ Some(_), _) => u
      case (None, Some(ldap)) => {
        ldap.lookup(sid).map(cn => UserDO(add(User(sid, cn)), sid, cn))
      }
      case _ => None
    }
  }

  def roles(sid: String) = DB.withSession { implicit session =>
    val rdos = Query(Users).filter(_.sid === sid).flatMap(_.roles).list
    rdos.map(rdo => Role.get(rdo.id)).collect { case Some(a) => a }
  }

  def hasRole(sid: String, role: Role) = roles(sid) contains role

  // Insert role, insert user-role
  /**
   * @return either Right(id) of the new role, or Left(errors).
   */
  def addRole(sid: String, role: Role) = DB.withSession { implicit session =>

    def insertUserRole(user: Long, role: Long) = {
      Query(UserRoles).filter(ur => ur.roleId === role && ur.userId === user).firstOption.map(ur => Right(ur._1)).getOrElse {
        val either = nonFatalCatch.either(models.dao.UserRoles.insert((user, role)))
        either.left.map(a => Seq(a.getMessage()))
      }
    }

    for {
      user <- getOrFetchLdap(sid).toRight(Seq("User " + sid + " does not exist in the system.")).right
      insertedRole <- models.Role.add(role).right
      insertedUserRole <- insertUserRole(user.id, insertedRole).right
    } yield insertedRole
  }

  /**
   * Remove a role from a user. If successful, return the number of rows returned, and get worried if it's more than 1.
   */
  def removeRole(sid: String, role: Role) = DB.withSession { implicit session =>
    for {
      u <- Query(Users).filter(_.sid === sid).map(_.id).firstOption
      r <- Role.find(role)
      ur = Query(UserRoles).filter(a => a.roleId === r && a.userId === u).delete
    } yield ur
  }
}