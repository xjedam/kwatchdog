package model

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.bson.types.ObjectId

class ServerSpec extends Specification {
  val uid = model.User.createUser("TestUser123abc", "regular", "en")
  val userData = new model.User(new ObjectId(uid.toString), "TestUser123abc", "regular", "en", false)
  val serverData = new model.Server(new ObjectId(), "212.212.212.212", "TestServer123abc", "Gdansk, Gnilna 2", "Details", userData._id, "ping", 666)
  "Server" should {
    
    "Create a server" in {
      running(FakeApplication()) {
        model.Server.getServer(serverData._id) must equalTo(None)
        val sid = model.Server.createServer(serverData.ip, serverData.name, serverData.location, serverData.details, serverData.user_id, serverData.method, serverData.period)
        model.Server.getServer(new ObjectId(sid.toString)) must not be None
      }
    }
    
    "Show users servers" in {
      running(FakeApplication()) {
        model.Server.createServer(serverData.ip, serverData.name + "2", serverData.location, serverData.details, serverData.user_id, serverData.method, serverData.period)
        val servers = model.Server.getUsersServers(serverData.user_id)
        servers must have length(2)
      }
    }
    
    "Delete a server" in {
      running(FakeApplication()) {
        val servers = model.Server.getUsersServers(serverData.user_id)
        servers.length must be_>(0)
        
        servers.foreach(serv => model.Server.delete(serv._id))
        
        val serversAfter = model.Server.getUsersServers(serverData.user_id)
        serversAfter.length must equalTo(0)
      }
    }
    
    "Show server" in {
      running(FakeApplication()) {
        val sid = model.Server.createServer(serverData.ip, serverData.name, serverData.location, serverData.details, serverData.user_id, serverData.method, serverData.period)
        val server = model.Server.getServer(new ObjectId(sid.toString))
        
        server.get.ip must equalTo(serverData.ip)
        server.get.name must equalTo(serverData.name)
        server.get.location must equalTo(serverData.location)
        server.get.details must equalTo(serverData.details)
        server.get.user_id must equalTo(serverData.user_id)
        server.get.method must equalTo(serverData.method)
        server.get.period must equalTo(serverData.period)
      }
    }
    
    "List all servers" in {
      running(FakeApplication()) {
        val servers = model.Server.getAllServers
        val sid1 = model.Server.createServer(serverData.ip, serverData.name, serverData.location, serverData.details, serverData.user_id, serverData.method, serverData.period)
        val sid2 = model.Server.createServer(serverData.ip, serverData.name + "2", serverData.location, serverData.details, serverData.user_id, serverData.method, serverData.period)
        val serversAfter = model.Server.getAllServers
        
        val serversCleanup = model.Server.getUsersServers(serverData.user_id)
        serversCleanup.foreach(serv => model.Server.delete(serv._id))
        
        serversAfter.length must equalTo(servers.length + 2)
      }
    }
    
    "Update server" in {
      running(FakeApplication()) {
        val sid1 = model.Server.createServer(serverData.ip, serverData.name, serverData.location, serverData.details, serverData.user_id, serverData.method, serverData.period)
        model.Server.saveServer(new model.Server(new ObjectId(sid1.toString), "121.121.121.121", "TestServer123abc", "Gdansk, Gnilna", "Details", userData._id, "ping", 666))       
        
        val server = model.Server.getServer(new ObjectId(sid1.toString))
        model.User.delete(userData._id)
        model.Server.delete(new ObjectId(sid1.toString))
        
        server must not be None
        server.get.ip must equalTo("121.121.121.121")
      }
    }
    
  }
}