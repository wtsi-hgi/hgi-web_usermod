package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in {
      running(FakeApplication()) {
        route(FakeRequest(GET, "/boum")) must beNone
      }
    }

    "render the index page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get

        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain("Your new application is ready.")
      }
    }

    "render the list of role types" in {
      running(FakeApplication()) {
        val roleTypes = route(FakeRequest(GET, "/roleTypes")).get

        status(roleTypes) must equalTo(OK)
        contentType(roleTypes) must beSome.which(_ == "application/json")
        contentAsString(roleTypes) must contain("set_global_role")
      }
    }

    "render the list of users" in {
      running(FakeApplication()) {
        val users = route(FakeRequest(GET, "/users")).get

        status(users) must equalTo(OK)
        contentType(users) must beSome.which(_ == "application/json")
        contentAsString(users) must contain("nc6")
      }
    }

    "insert a role, using route POST /routes" in {
      running(FakeApplication()) {
        val json = """
          {"name":"create_project","parameters":[]}
          """.trim

        val createRole = route(new FakeRequest(POST, "/roles",
          FakeHeaders(Seq("Content-Type" -> Seq("application/json"))),
          Json.parse(json))).get

        status(createRole) must equalTo(OK)
        contentType(createRole) must beSome("application/json")
      }
    }
  }
  
  "Role" should {
    import models._
    
    "serialise and deserialise correctly" in {
      val role = Role("testing_stuff", Seq(Parameter("with_this", "set to this")))
      val json = Json.stringify(Json.toJson(role))
      
      json.trim() mustEqual """{"name":"testing_stuff","parameters":[{"name":"with_this","value":"set to this"}]}"""
      
      val parsed = Json.fromJson[Role](Json.parse(json)).asOpt
      parsed must beSome.which(_ == role)
      
    }
  }
}