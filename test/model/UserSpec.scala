package model

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class UserSpec extends Specification {
  val userData = ("TestUser123abc", "regular", "en")
  "User" should {
    
    "Create a user" in {
      running(FakeApplication()) {
        model.User.getUser(userData._1) must equalTo(None)
        val user = model.User.createUser(userData._1, userData._2, userData._3)  
        model.User.getUser(userData._1) must not be None
      }
    }
    
    "Show user" in {
      running(FakeApplication()) {
        val user = model.User.getUser(userData._1)
        user must not be None
        user.get.login must equalTo(userData._1)
        user.get.role must equalTo(userData._2)
        user.get.lang must equalTo(userData._3)
      }
    }
    
    "Delete a user" in {
      running(FakeApplication()) {
        val user = model.User.getUser(userData._1)
        user must not be None
        model.User.delete(user.get._id)
        model.User.getUser(userData._1) must equalTo(None)
      }
    }
    
    "List all users" in {
      running(FakeApplication()) {
        val startNum = model.User.getAllUsers.length
        
        model.User.createUser(userData._1 + "1", userData._2, userData._3)
        model.User.createUser(userData._1 + "2", userData._2, userData._3)
        model.User.createUser(userData._1 + "3", userData._2, userData._3)
        
        model.User.getAllUsers.length must equalTo(startNum + 3)
        model.User.delete(model.User.getUser(userData._1 + "1").get._id)
        model.User.delete(model.User.getUser(userData._1 + "2").get._id)
        model.User.delete(model.User.getUser(userData._1 + "3").get._id)
        model.User.getAllUsers.length must equalTo(startNum)
      }
    }
    
  }

}