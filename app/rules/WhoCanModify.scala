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