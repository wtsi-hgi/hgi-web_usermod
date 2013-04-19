package models
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json.Json

case class User(id : String)

// Will look up values from LDAP (and cache?)
object User {
    
  implicit val writes = Json.writes[User]
  
  val user = {
    get[String]("id") map { case id => User(id) }
  }
  
  /**
   * Get a list of all users.
   */
  def all() : List[User] = DB.withConnection { implicit c =>
    SQL("select * from user").as(user *)    
  }
  
  /**
   * Get a list of all roles a user has.
   */
  // TODO
  def roles(userId : String) = DB.withConnection { implicit c =>
    SQL(
      """
        select 
      """)
  }

  
}