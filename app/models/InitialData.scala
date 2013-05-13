package models

import play.api._
import models._
import models.dao._

object InitialData {

  val initialParameterTypes = Seq(
    ParameterType("project", Some("Project role")))

  val initialRoleTypes = Seq(
    RoleType("create_project", Some("Can create projects."), Nil),
    RoleType("set_global_role", Some("Can set roles globally."), Nil),
    RoleType("delegate", Some("Can delegate permissions for this project."), initialParameterTypes),
    RoleType("manage_project_users", Some("Can manage users for this project."), initialParameterTypes),
    RoleType("manage_project_datasets", Some("Can manage datasets for this project."), initialParameterTypes),
    RoleType("manage_project_resources", Some("Can manage resources for this project."), initialParameterTypes))
    
  val initialUsers = Seq(
    UserDO(1L, "nc6@sanger.ac.uk"))

  def insert() {
    if (ParameterTypes.count == 0 && RoleTypes.count == 0) {
      initialRoleTypes.foreach(RoleType.insert)
    }

    if (Users.count == 0) {
      initialUsers.foreach(Users.insert)
      initialUsers.foreach(a => User.addRole(a.sid, initialRoleTypes.head.instantiate(Nil : Seq[Parameter]).get))
    }
  }

}