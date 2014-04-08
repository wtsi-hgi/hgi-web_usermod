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

import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import TupleMethods._

/**
 * Role type is the prototype for a role.
 */
private[models] case class RoleTypeDO(id: Long, name: String, description: Option[String])

private[models] class RoleTypes(tag : Tag) extends Table[RoleTypeDO](tag, "ROLE_TYPES") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def description = column[String]("description", O.Nullable)

  def * = id ~ name ~ description.? <> (RoleTypeDO.tupled, RoleTypeDO.unapply)
  
  def nameIdx = index("idx_name", name, unique = false)
  
  def parameters = RoleTypeParameterTypes.filter(_.rtId === id).flatMap(_.parameterType)
}

private[models] class RoleTypeParameterTypes(tag : Tag) extends Table[(Long, Long)](tag, "ROLE_TYPE_PARAMETER_TYPE") {
  def rtId = column[Long]("role_type_id", O.NotNull)
  def ptId = column[Long]("parameter_type_id", O.NotNull)
  def * = rtId ~ ptId

  def roleType = foreignKey("rtpt_role_type_fk", rtId, RoleTypes)(_.id)
  def parameterType = foreignKey("rtpt_parameter_type_fk", ptId, ParameterTypes)(_.id)
}