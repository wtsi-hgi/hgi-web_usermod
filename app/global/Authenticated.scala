package global

import java.util.Date
import org.apache.commons.codec.binary.Base64
import play.api.mvc._
import play.api.mvc.Security.{ Authenticated => PlayAuth }
import play.api.Logger
import controllers.routes
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Date
import play.api.Play.current

/**
 * Authentication trait using OAuth 2.0 style Bearer tokens.
 *
 * Format is base64($user:$expiration:$shib_session_id:$salt:$mac) where $mac is the HMAC-MD5 of the rest of it.
 */
abstract class Authenticated(keyString: String) {
  
  protected[this] case class Token(user: String, expires: Date, shib: String, salt: String, mac: String)

  protected[this] object L {
    def unapply(s: String): Option[Long] = try {
      Some(s.toLong)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }

  // Maybe a bit nicer?
  protected[this] def getToken(auth: String) = auth.drop(7)

  protected[this] def decodeToken(token: String) = {
    val parts = new String(Base64.decodeBase64(token)).split(":").toList
    parts match {
      case (user :: (L(expires) :: shib :: salt :: mac :: Nil)) => Some(Token(user, new Date(expires * 1000), shib, salt, mac))
      case _ => None
    }
  }

  protected[this] def hmac(raw: Array[Byte]) = {
    val key = new SecretKeySpec(keyString.getBytes(), "HmacMD5")
    val mac = Mac.getInstance("HmacMD5")
    mac.init(key)
    mac.doFinal(raw)
  }

  protected[this] def verifyHmac(token: Token) = {
    Logger.debug(s"Token: $token")
    val now = (new java.util.Date).getTime()
    val timeRemaining = token.expires.getTime - now
    (timeRemaining > 0) && {
      Logger.debug(s"Auth token has $timeRemaining milliseconds remaining.")
      val raw = s"${token.user}:${token.expires.getTime() / 1000}:${token.shib}:${token.salt}"
      Base64.decodeBase64(token.mac).sameElements(hmac(raw.getBytes()))
    }
  }

  protected[this] def username(request: RequestHeader) = for {
    auth <- request.headers.get("Authorization")
    token <- decodeToken(getToken(auth)) if verifyHmac(token)
  } yield token.user

  // TODO work out unauthorized behaviour.
  protected[this] def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.index)

  /**
   * Wrap the given action in an authentication context. It will only be executed if successful authentication is presented.
   */
  def authenticated(f: => String => EssentialAction) = {
    PlayAuth(username, onUnauthorized) {
      user => f(user)
    }
  }
}

object Authenticated extends Authenticated(current.configuration.getString("hgiweb.secretkey").getOrElse("default"))