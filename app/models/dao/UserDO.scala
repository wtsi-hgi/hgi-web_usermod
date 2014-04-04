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
package models.dao

import java.sql.Date
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

private[models] case class UserDO(id: Long, sid: String, name : String)

private[models] class Users extends Table[UserDO]("USERS") with StandardQueries[UserDO] {
  
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def sid = column[String]("sid", O.NotNull)
  def name = column[String]("name")
  
  def * = id ~ sid ~ name <> (UserDO.apply _, UserDO.unapply _)
  def forInsert = sid ~ name returning id
  
  def roles = UserRoles.filter(_.userId === id).flatMap(_.role)
}

private[models] class UserRoles extends Table[(Long, Long)]("USER_ROLES") with StandardQueries[(Long, Long)] {
  def userId = column[Long]("user_id", O.NotNull)
  def roleId = column[Long]("role_id", O.NotNull)
  
  def * = userId ~ roleId
  
  def user = foreignKey("ur_user_fk", userId, Users)(_.id)
  def role = foreignKey("ur_role_fk", roleId, Roles)(_.id)
}