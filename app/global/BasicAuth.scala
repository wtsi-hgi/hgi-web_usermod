package global

import play.api.mvc.RequestHeader
import java.util.Date
import org.apache.commons.codec.binary.Base64
import play.api.mvc._
import play.api.mvc.Security.{ Authenticated => PlayAuth }
import play.api.Logger
import controllers.routes
import java.util.Date
import play.api.Play.current

class BasicAuth(val keyString: String) extends AuthProvider with Hmac {

  protected[this] object L {
    def unapply(s: String): Option[Long] = try {
      Some(s.toLong)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }

  // Maybe a bit nicer? At the moment, this just drops the 'Basic ' string to get the actual token.
  protected[this] def getToken(auth: String) = auth.drop(6)

  protected[this] def decodeToken(token: String) = {
    Logger.debug(s"Token: $token")
    val parts = new String(Base64.decodeBase64(token)).split(":").toList
    parts match {
      case (user :: password :: Nil) => {
        val parts2 = new String(Base64.decodeBase64(user)).split(":").toList
        parts2 match {
          case (user :: (L(expires) :: shib :: salt :: Nil)) => Some(Token(user, new Date(expires * 1000), shib, salt, password))
          case _ => {
            Logger.debug(s"Cannot parse password: $password")
            None
          }
        }
      }
      case _ => {
        Logger.debug(s"Cannot parse token: $token")
        None
      }
    }
  }

  def username(request: RequestHeader) = for {
    auth <- request.headers.get("Authorization")
    token <- decodeToken(getToken(auth)) if verifyHmac(token)
  } yield token.user

  def onUnauthorized(request: RequestHeader) = _.withHeaders("WWW-Authenticate" -> "Basic realm=\"hgi-web\"")
}