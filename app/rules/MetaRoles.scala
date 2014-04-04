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

object MetaRoles {
  
  val all = Seq(set_global_role, delegate, grant_project_role, create_project)

  /**
   * Users with the 'set_global_role' role can do pretty much anything, apart from removing it from themselves!
   */
  val set_global_role = RoleType("set_global_role", Some("Users with the 'set_global_role' role can do pretty much anything, apart from removing it from themselves!"), Seq())

  /**
   * Users with the delegate role for a given project can delegate any role they already have on that project to another user.
   */
  val delegate = RoleType("delegate",
    Some("Users with the delegate role for a given project can delegate any role they already have on that project to another user."),
    Seq(ParameterType("project", Some("Project."))))

  val grant_project_role = RoleType("grant_project_role",
    Some("Users with this role may grant the given project role on the specified project."),
    Seq(ParameterType("project", Some("Project.")), ParameterType("role", Some("Name of the corresponding role."))))

  /**
   * Users with the 'create_project' role can create new projects.
   */
  val create_project = RoleType("create_project",
    Some("Users with the 'create_project' role can create new projects."), Seq())

}