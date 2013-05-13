package models

import play.api._
import models._
import models.dao._

  object InitialData {

  val initialParameterTypes = Seq(
    ParameterTypeDO(1L, "project", Some("Project role")))

  val initialRoleTypes = Seq(
    RoleTypeDO(1L, "create_project", Some("Can create projects.")),
    RoleTypeDO(2L, "set_global_role", Some("Can set roles globally.")),
    RoleTypeDO(3L, "delegate", Some("Can delegate permissions for this project.")),
    RoleTypeDO(4L, "manage_project_users", Some("Can manage users for this project.")),
    RoleTypeDO(5L, "manage_project_datasets", Some("Can manage datasets for this project.")),
    RoleTypeDO(6L, "manage_project_resources", Some("Can manage resources for this project.")))
    
  val initialRoleTypeParameterTypes = Seq(
    (3L, 0L),
    (4L, 0L),
    (5L, 0L),
    (6L, 0L)
  )

  val initialUsers = Seq(
    UserDO(1L, "nc6@sanger.ac.uk"))
    
  val initialRoles = Seq(
    RoleDO(1L, 2L)
  )
  
  val initialUserRoles = Seq(
      (1L, 1L)
  )

  def insert() {
    if (ParameterTypes.count == 0 && RoleTypes.count == 0) {
      initialParameterTypes.foreach(ParameterTypes.insert)
      initialRoleTypes.foreach(RoleTypes.insert)
      initialRoleTypeParameterTypes.foreach(RoleTypeParameterTypes.insert)
      initialRoles.foreach(Roles.insert)
    }
    
    if (Users.count == 0) {
      initialUsers.foreach(Users.insert)
      initialUserRoles.foreach(UserRoles.insert)
    }
  }

}