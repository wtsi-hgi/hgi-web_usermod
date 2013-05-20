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

trait AuthProvider {
  def username(request: RequestHeader): Option[String]
  def onUnauthorized(request: RequestHeader): Result => Result
}

trait Hmac {
  val keyString : String
  
    protected[this] case class Token(user: String, expires: Date, shib: String, salt: String, mac: String)
    
    protected[this] def hmac(raw: Array[Byte]) = {
    val key = new SecretKeySpec(Base64.decodeBase64(keyString), "HmacMD5")
    val mac = Mac.getInstance("HmacMD5")
    mac.init(key)
    mac.doFinal(raw)
  }

  protected[this] def verifyHmac(token: Token) = {
    val now = (new java.util.Date).getTime()
    val timeRemaining = token.expires.getTime - now
    if (timeRemaining < 0) {
      Logger.debug("Token has expired on " + token.expires)
      false
    } else {
      Logger.debug(s"Auth token has $timeRemaining milliseconds remaining.")
      val raw = s"${token.user}:${token.expires.getTime() / 1000}:${token.shib}:${token.salt}"
      Base64.decodeBase64(token.mac).sameElements(hmac(raw.getBytes()))
    }
  }
}

abstract class Authenticated(authProviders: AuthProvider*) {

  private[this] def username(request: RequestHeader): Option[String] = authProviders.map(_.username(request)).reduceLeft(_.orElse(_))
  private[this] def onUnauthorized(request: RequestHeader) = ((Results.Unauthorized: Result) /: authProviders) { case (r, f) => f.onUnauthorized(request)(r) }

  /**
   * Wrap the given action in an authentication context. It will only be executed if successful authentication is presented.
   */
  def authenticated(f: => String => EssentialAction) = {
    PlayAuth(username, onUnauthorized) {
      user => f(user)
    }
  }
}

object Authenticated extends Authenticated(new BearerAuth(current.configuration.getString("hgiweb.secretkey").getOrElse("default")))