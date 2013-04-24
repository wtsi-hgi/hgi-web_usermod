package object models {
  import dao.ParameterTypeDO
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  // Re-export the DO for now.
  type ParameterType = ParameterTypeDO
}