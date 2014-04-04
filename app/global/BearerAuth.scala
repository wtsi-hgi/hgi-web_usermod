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