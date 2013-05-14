package global

import javax.naming._
import javax.naming.directory.{ SearchControls, InitialDirContext }
import scala.collection.JavaConversions.mapAsJavaMap

class LDAPProvider(serverUrl: String) {

  val context = {
    val env = Map(
      Context.PROVIDER_URL -> serverUrl,
      Context.INITIAL_CONTEXT_FACTORY -> "com.sun.jndi.ldap.LdapCtxFactory")

    val ht = new java.util.Hashtable[String, String]()
    ht.putAll(mapAsJavaMap(env))
    new InitialDirContext(ht)
  }

  val searchControls = new SearchControls
  searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE)

  def lookup(sid: String) = {
    val search = context.search("ou=people,dc=sanger,dc=ac,dc=uk", s"mail=$sid", searchControls)
    if (search.hasMoreElements()) {
      val elt = search.next()
      Some(sid)
    } else {
      None
    }
  }
}