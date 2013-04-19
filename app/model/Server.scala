package model

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject

case class Server(
  _id: ObjectId,
  ip: String,
  name: String,
  location: String,
  details: String,
  user_id: ObjectId)

object Server extends Entity("servers") {

  def getServer(id: ObjectId) = {
    getOne(MongoDBObject("_id" -> id)) match {
      case Some(u: DBObject) => Some(new Server(u.get("_id").asInstanceOf[ObjectId], u.get("ip").asInstanceOf[String],
        u.get("name").asInstanceOf[String], u.get("location").asInstanceOf[String], u.get("details").asInstanceOf[String], 
        u.get("user_id").asInstanceOf[ObjectId]))
      case _ => None
    }
  }

  def createServer(ip: String, name: String, location: String, details: String, uid: ObjectId) = {
    save(MongoDBObject("ip" -> ip, "name" -> name, "location" -> location, "details" -> details, "user_id" -> uid))
  }

}