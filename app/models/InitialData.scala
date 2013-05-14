package models

import play.api._
import models._
import models.dao._
import rules.MetaRoles

object InitialData {

  val set_global_role = MetaRoles.set_global_role.instantiate(Map.empty[String, String]).get

  val initialParameterTypes = Seq(
    ParameterType("project", Some("Project role")))

  val initialRoleTypes = Seq(
    MetaRoles.set_global_role,
    MetaRoles.create_project,
    MetaRoles.delegate,
    RoleType("manage_project_users", Some("Can manage users for this project."), initialParameterTypes),
    RoleType("manage_project_datasets", Some("Can manage datasets for this project."), initialParameterTypes),
    RoleType("manage_project_resources", Some("Can manage resources for this project."), initialParameterTypes))

  val initialUsers = Seq(
    UserDO(1L, "nc6@sanger.ac.uk"))

  val initialUserRoles = Seq(
    "nc6@sanger.ac.uk" -> set_global_role)

  def insert() {
    if (ParameterTypes.count == 0 && RoleTypes.count == 0) {
      initialRoleTypes.foreach(RoleType.insert)
    }

    if (Users.count == 0) {
      initialUsers.foreach(Users.insert)
      initialUserRoles.foreach(Function.tupled(User.addRole))
    }
  }

}