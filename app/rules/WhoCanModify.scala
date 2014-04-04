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
package rules

import models._

object WhoCanModify {

  val set_global_role = MetaRoles.set_global_role.instantiate(Map.empty[String, String]).get

  def canDo(user: String, to: String, what: Role): Boolean = {
    // Users with the 'set_global_role' role can do pretty much anything, apart from remove this role from themselves.
    if (canSetGlobalRole(user) && !(to == user && what == set_global_role)) {
      true
    } else {
      // user can assign what to to if:
      // user has role 'what'
      // user has role 'delegate project' for the project of 'what'
      // user has role 'grant_project_role' for the project of 'what' and for the given role.
      val project = what.parameters.find(_.name == "project") // Project role
      project match {
        case Some(p) => {
          val delegate_project = MetaRoles.delegate.instantiate(Seq(p)).get
          val grant_project_role = MetaRoles.grant_project_role.instantiate(Seq(p, Parameter("role", what.name))).get
          if (User.hasRole(user, what) && User.hasRole(user, delegate_project)) {
            true
          } else if (User.hasRole(user, grant_project_role)) {
            true
          } else {
            false
          }
        }
        case None => false
      }
    }
  }

  def canSetGlobalRole(who: String) = {
    User.hasRole(who, set_global_role)
  }

}