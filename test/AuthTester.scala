import org.apache.commons.codec.binary.Base64._
import global.Authenticated

class AuthTester(key: String) extends Authenticated(key) {

  def verify(raw: String) = decodeToken(raw).map(verifyHmac)

  def encode(raw: String) = hmac(raw.getBytes)

  def fullEncode(raw: String) = encodeBase64((raw + ":" + encodeBase64String(encode(raw))).getBytes)

}