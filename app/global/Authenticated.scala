/*
Copyright (c) 2013, Wellcome Trust Sanger Institute

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of the Wellcome Trust Sanger Institute nor the 
      names of other contributors may be used to endorse or promote 
      products derived from this software without specific prior written 
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
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
  val keyString: String

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

object Authenticated extends Authenticated(new BearerAuth(current.configuration.getString("hgiweb.secretkey").getOrElse("default")),
  new BasicAuth(current.configuration.getString("hgiweb.secretkey").getOrElse("default")))