package models
import play.api.db._
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

case class User(id: Long, sid: String)

// Will look up values from LDAP (and cache?)
object User {

  implicit val writes = Json.writes[User]

  // Queries
  def all() = DB.withSession { implicit session =>
    Query(Users).list
  }
}

object Users extends Table[User]("USERS") {
  def id = column[Long]("id", O.PrimaryKey)
  def sid = column[String]("sid", O.NotNull)

  def * = id ~ sid <> (User.apply _, User.unapply _)
}