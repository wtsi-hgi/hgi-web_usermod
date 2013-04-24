package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Parameter(name : String, value : String)
object Parameter {
  implicit val toJson = Json.writes[Parameter]
}