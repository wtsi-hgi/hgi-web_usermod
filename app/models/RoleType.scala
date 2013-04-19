package models

/**
 * A parameter to a role type.
 */
case class ParameterType(name : String, description : String)

/**
 * Role type is the prototype for a role.
 */
case class RoleType(name : String, description : String, parameters : Seq[ParameterType])