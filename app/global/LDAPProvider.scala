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