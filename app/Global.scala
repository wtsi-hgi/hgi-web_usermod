import play.api._

import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }

}

object InitialData {

  val initialParameterTypes = Seq(
    ParameterType(0L, "project", Some("Project role")))

  val initialRoleTypes = Seq(
    RoleType(0L, "create_project", Some("Can create projects.")),
    RoleType(1L, "set_global_role", Some("Can set roles globally.")),
    RoleType(2L, "delegate", Some("Can delegate permissions for this project.")),
    RoleType(3L, "manage_project_users", Some("Can manage users for this project.")))
    
  val initialRoleTypeParameterTypes = Seq(
    (2L, 0L),
    (3L, 0L)
  )
  

  val initialUsers = Seq(
    User(0L, "nc6"))

  def insert() {
    if (ParameterTypes.count == 0) {
      initialParameterTypes.foreach(ParameterTypes.insert)
    }
    if (RoleTypes.count == 0) {
      initialRoleTypes.foreach(RoleTypes.insert)
    }
    if (RoleTypeParameterTypes.count == 0) {
      initialRoleTypeParameterTypes.foreach(RoleTypeParameterTypes.insert)
    }
    
  }

}