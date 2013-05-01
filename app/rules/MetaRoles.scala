package rules

import models._

object MetaRoles {

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

  /**
   * Users with the 'create_project' role can create new projects.
   */
  val create_project = RoleType("create_project",
    Some("Users with the 'create_project' role can create new projects."), Seq())

}