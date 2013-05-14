package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.bson.types.ObjectId
import play.api.i18n.Messages
import play.api.i18n.Lang
import controllers.security.Secured
import play.api.mvc.Cookies
import play.api.mvc.Security

class ApplicationSpec extends Specification {
  override def is = args(sequential = true) ^ super.is
  val uid = model.User.createUser("TestUserApp", "regular", "en")
  val aid = model.User.createUser("TestAdminApp@kainos.com", "admin", "en")
  val userData = new model.User(new ObjectId(uid.toString), "TestUserApp", "regular", "en", false)
  val adminData = model.User.getUser(new ObjectId(aid.toString)).get
  val sid = model.Server.createServer("212.212.212.212", "TestServerApp", "Gdansk Gnilna 2", "Details", userData._id, "ping", 666)
  val serverData = model.Server.getServer(new ObjectId(sid.toString)).get
  
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
        contentAsString(home) must contain ("KWatchdog")
      }
    }
    
    "render the change lang to pl page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/users/lang/pl")).get
        
        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain (Messages("app.langChanged")(Lang("pl")))
      }
    }
    
    "render the change lang to en page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/users/lang/en")).get
        
        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain (Messages("app.langChanged")(Lang("en")))
      }
    }
    
    "render the login page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/login")).get
        
        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain (Messages("auth.login")(Lang("en")))
      }
    }
    
    "render the logout page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/logout")).get
        
        status(home) must equalTo(SEE_OTHER)
        flash(home).get("success").getOrElse("") must contain (Messages("auth.loggedout")(Lang("en")))
      }
    }
    
    "render the register page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/users/new")).get
        
        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain (Messages("app.newUser")(Lang("en")))
      }
    }
    
    "allow admin user controll" in {
      running(FakeApplication()) {
        val uid2 = model.User.createUser("TestUserApp2", "regular", "en")
        
        //checking invalid login
        val badResult = controllers.Auth.authenticate(FakeRequest()) 
        status(badResult) must equalTo(BAD_REQUEST)
        
        //checking normal login
        val result = controllers.Auth.authenticate(
          FakeRequest().withFormUrlEncodedBody("login" -> adminData.login, "password" -> "qwerasdf")
        )
        status(result) must equalTo(SEE_OTHER)
        
        //listing users as admin
        val usersRes = route(FakeRequest(GET, "/users").withSession(Security.username -> adminData.login)).get
        status(usersRes) must equalTo(OK)
        contentAsString(usersRes) must contain(userData.login)
        
        //entering edit page as admin
        val usersEditRes = route(FakeRequest(GET, "/users/TestUserApp2/edit").withSession(Security.username -> adminData.login)).get

        status(usersRes) must equalTo(OK)
        contentAsString(usersRes) must contain(userData.login)
        contentAsString(usersRes) must contain(Messages("app.manageUser")(Lang("en")))
        
        //editing a user
        val result2 = route(FakeRequest(POST, "/users/" + uid2.toString + "/update")
            .withFormUrlEncodedBody("login" -> "TestUserApp2edited", "role" -> "viewer", "lang" -> "en", "disabled" -> "false")
            .withSession(Security.username -> adminData.login)).get
        status(result2) must equalTo(SEE_OTHER)
        flash(result2).get("success").getOrElse("") must contain (Messages("auth.userEdited")(Lang("en")))
        
        //deleting user as admin
        val usersDeleteRes = route(FakeRequest(GET, "/users/TestUserApp2edited/delete").withSession(Security.username -> adminData.login)).get

        status(usersDeleteRes) must equalTo(SEE_OTHER)
        flash(usersDeleteRes).get("success").getOrElse("") must contain(Messages("auth.userDeleted")(Lang("en")))
      }
    }
    
    "list all servers" in {
      running(FakeApplication()) {
        val result = route(FakeRequest(GET, "/servers").withSession(Security.username -> adminData.login)).get
        status(result) must equalTo(OK)
        contentAsString(result) must contain(serverData.name)
      }
    }
    
    "view a server" in {
      running(FakeApplication()) {
        val result = route(FakeRequest(GET, "/servers/" + serverData._id + "/view").withSession(Security.username -> adminData.login)).get
        status(result) must equalTo(OK)
        contentAsString(result) must contain(serverData.name)
      }
    }
    
    "view users servers" in {
      running(FakeApplication()) {
        val result = route(FakeRequest(GET, "/servers/" + userData._id + "/list").withSession(Security.username -> userData.login)).get
        status(result) must equalTo(OK)
        contentAsString(result) must contain(serverData.name)
      }
    }
    
    "allow editing servers" in {
      running(FakeApplication()) {
        val result = route(FakeRequest(GET, "/servers/" + serverData._id + "/edit").withSession(Security.username -> adminData.login)).get
        status(result) must equalTo(OK)
        contentAsString(result) must contain(serverData.name)
        
        val result2 = route(FakeRequest(POST, "/servers/" + serverData._id + "/update")
            .withFormUrlEncodedBody("ip" -> serverData.ip, "name" -> (serverData.name + "Edited"), "location" -> serverData.location,
                "details" -> serverData.details, "method" -> serverData.method, "period" -> "321")
            .withSession(Security.username -> adminData.login)).get
        status(result2) must equalTo(SEE_OTHER)
        flash(result2).get("success").getOrElse("") must contain (Messages("serv.updated")(Lang("en")))
        
        val result3 = route(FakeRequest(GET, "/servers/" + serverData._id + "/view").withSession(Security.username -> adminData.login)).get
        status(result3) must equalTo(OK)
        contentAsString(result3) must contain(serverData.name + "Edited")
      }
    }
    
    "allow deleting servers" in {
      running(FakeApplication()) {
        val result = route(FakeRequest(GET, "/servers/" + serverData._id + "/delete").withSession(Security.username -> adminData.login)).get
        status(result) must equalTo(SEE_OTHER)
        flash(result).get("success").getOrElse("") must contain (Messages("serv.deleted")(Lang("en")))
        
        val result2 = route(FakeRequest(GET, "/servers/" + serverData._id + "/view").withSession(Security.username -> adminData.login)).get
        status(result2) must equalTo(SEE_OTHER)
        flash(result2).get("error").getOrElse("") must contain (Messages("app.notFound")(Lang("en")))
        
        model.User.delete(userData._id)
        model.User.delete(adminData._id)
        model.Server.delete(serverData._id)
        true
      }
    }
  }
}