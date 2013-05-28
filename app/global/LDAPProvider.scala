package global

import javax.naming._
import javax.naming.directory.{ SearchControls, InitialDirContext }
import scala.collection.JavaConversions.{ enumerationAsScalaIterator, mapAsJavaMap }

import play.api.Play.current
import play.api.cache.Cache
import play.api.Logger

class LDAPProvider(serverUrl: String) {

  val context = {
    val env = Map(
      Context.PROVIDER_URL -> serverUrl,
      Context.INITIAL_CONTEXT_FACTORY -> "com.sun.jndi.ldap.LdapCtxFactory")

    val ht = new java.util.Hashtable[String, String]()
    ht.putAll(mapAsJavaMap(env))
    new InitialDirContext(ht)
  }

  // Yuck yuck yuck.
  def lookup(sid: String) = {
    val searchControls = new SearchControls
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE)
    val search = context.search("ou=people,dc=sanger,dc=ac,dc=uk", s"mail=$sid", searchControls)
    if (search.hasMoreElements()) {
      val a = search.nextElement().getAttributes()
      val cn = a.get("cn")
      if (null != cn) {
        cn.get() match {
          case x: String => Some(x)
          case _ => None
        }
      } else {
        None
      }
    } else {
      None
    }
  }

  def search(pattern: String) = {
    val allUsers = Cache.getOrElse[Seq[String]]("ldap.users") {
      Logger.debug("No LDAP userlist in cache, fetching from LDAP server.")
      val searchControls = new SearchControls
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE)
      searchControls.setReturningAttributes(Array("mail"))
      val search = context.search("ou=people,dc=sanger,dc=ac,dc=uk", "(sangerRealPerson=TRUE)", searchControls)

      (for {
        res <- search
        attr <- res.getAttributes().getAll() if attr.getID() == "mail"
      } yield {
        attr.get().toString()
      }).toSeq
    }

    allUsers.filter(_.startsWith(pattern))
  }
}