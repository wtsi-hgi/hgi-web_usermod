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

import dao.ParameterTypes
import dao.ParameterTypeDO

case class ParameterType(name : String, description : Option[String]) {
  def instantiate(value : String) = Parameter(name, value)
}

object ParameterType { 
  implicit val toJson = Json.writes[ParameterType]
  implicit val fromJson = Json.reads[ParameterType]
  
  def get(name : String) = DB.withSession { implicit session =>
    ParameterTypes.filter(_.name === name).firstOption
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
    val ptdo = ParameterTypeDO(1, pt.name, pt.description)
    ParameterTypes insert ptdo
  }
}