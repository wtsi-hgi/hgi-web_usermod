package rules

import models._

object WhoCanModify {
  
  val set_global_role = MetaRoles.set_global_role.instantiate(Map.empty[String, String]).get

  def canDo(user: User, to: User, what: Role): Boolean = {
    // Users with the 'set_global_role' role can do pretty much anything, apart from remove this role from themselves.
    if (canSetGlobalRole(user.sid) && !(to == user && what == set_global_role)) {
      true
    } else {
      // user can assign what to to if:
      // user has role 'what'
      // user has role 'delegate project' for the project of 'what'
      val project = what.parameters.find(_.name == "project") // Project role
      project match {
        case Some(p) => {
          val delegate_project = MetaRoles.delegate.instantiate(Seq(p)).get
          if (User.hasRole(user.sid, what) && User.hasRole(user.sid, delegate_project)) {
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