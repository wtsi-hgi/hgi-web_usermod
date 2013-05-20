package global

import java.util.Date
import org.apache.commons.codec.binary.Base64
import play.api.mvc._
import play.api.mvc.Security.{ Authenticated => PlayAuth }
import play.api.Logger
import controllers.routes
import java.util.Date
import play.api.Play.current

/**
 * Authentication trait using OAuth 2.0 style Bearer tokens.
 *
 * Format is base64($user:$expiration:$shib_session_id:$salt:$mac) where $mac is the HMAC-MD5 of the rest of it.
 */
class BearerAuth(val keyString: String) extends AuthProvider with Hmac {
  
  protected[this] object L {
    def unapply(s: String): Option[Long] = try {
      Some(s.toLong)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }

  // Maybe a bit nicer? At the moment, this just drops the 'Bearer' string to get the actual token.
  protected[this] def getToken(auth: String) = auth.drop(7)

  protected[this] def decodeToken(token: String) = {
    Logger.debug(s"Token: $token")
    val parts = new String(Base64.decodeBase64(token)).split(":").toList
    parts match {
      case (user :: (L(expires) :: shib :: salt :: mac :: Nil)) => Some(Token(user, new Date(expires * 1000), shib, salt, mac))
      case _ => {
        Logger.debug("Cannot parse token: " + token)
        None
      }
    }
  }

  def username(request: RequestHeader) = for {
    auth <- request.headers.get("Authorization")
    token <- decodeToken(getToken(auth)) if verifyHmac(token)
  } yield token.user

  def onUnauthorized(request: RequestHeader) = _.withHeaders("WWW-Authenticate" -> "Bearer error=\"invalid_token\"")

}