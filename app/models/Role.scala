package models

case class Parameter(id : Integer, parameterType : ParameterType, value : String)

case class Role(id : Integer, roleType : RoleType, parameters : Seq[Parameter])

object Role {
  def all() : List[Role] = Nil
  def create(roleType : RoleType, parameters : Seq[String]) {}
  def delete(name : String) {}
}