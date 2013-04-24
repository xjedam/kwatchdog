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
  user_id: ObjectId,
  method: String,
  period: Int)

object Server extends Entity("servers") {
  
  val checkMethods = Map("ping" -> "serv.pingCheck", "logs" -> "serv.logCheck", "appResp" -> "serv.appCheck")
  
  def mapToServer(o: Option[DBObject]) = o match {
    case Some(u: DBObject) => Some(new Server(u.get("_id").asInstanceOf[ObjectId], u.get("ip").asInstanceOf[String],
        u.get("name").asInstanceOf[String], u.get("location").asInstanceOf[String], u.get("details").asInstanceOf[String], 
        u.get("user_id").asInstanceOf[ObjectId], u.get("method").asInstanceOf[String], u.get("period").asInstanceOf[Int]))
    case _ => None
  }

  def getServer(id: ObjectId) = mapToServer(getOne(MongoDBObject("_id" -> id)))
  
  def getUsersServer(uid: ObjectId) = get(MongoDBObject("user_id" -> uid)).map{obj: DBObject => mapToServer(Some(obj)).get}

  def createServer(ip: String, name: String, location: String, details: String, uid: ObjectId, method: String, period: Int) = {
    save(MongoDBObject("ip" -> ip, "name" -> name, "location" -> location, "details" -> details, "user_id" -> uid,
        "method" -> method, "period" -> period))
  }
  
  def saveServer(server: model.Server) = save(MongoDBObject("ip"-> server.ip, "_id" -> server._id, "name" -> server.name, "location" -> server.location,
      "details" -> server.details, "method" -> server.method, "period" -> server.period, "user_id" -> server.user_id))

}