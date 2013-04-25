package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

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
  }
}