package models

import play.api._
import models._
import models.dao._

  object InitialData {

  val initialParameterTypes = Seq(
    ParameterTypeDO(0L, "project", Some("Project role")))

  val initialRoleTypes = Seq(
    RoleTypeDO(0L, "create_project", Some("Can create projects.")),
    RoleTypeDO(1L, "set_global_role", Some("Can set roles globally.")),
    RoleTypeDO(2L, "delegate", Some("Can delegate permissions for this project.")),
    RoleTypeDO(3L, "manage_project_users", Some("Can manage users for this project.")))
    
  val initialRoleTypeParameterTypes = Seq(
    (2L, 0L),
    (3L, 0L)
  )

  val initialUsers = Seq(
    UserDO(0L, "nc6"))

  def insert() {
    if (ParameterTypes.count == 0 && RoleType.count == 0) {
      initialParameterTypes.foreach(ParameterTypes.insert)
      initialRoleTypes.foreach(RoleTypes.insert)
      initialRoleTypeParameterTypes.foreach(RoleTypeParameterTypes.insert)
    }
    
    if (Users.count == 0) {
      initialUsers.foreach(Users.insert)
    }
  }

}