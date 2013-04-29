package model

import org.bson.types.ObjectId
import java.util.Date
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject

case class ServerStatus (
    _id: ObjectId,
    server_id: ObjectId,
    date: Date,
    online: Boolean,
    info: String)
    
object ServerStatus extends Entity("status") {
  
  def mapToStatus(o: Option[DBObject]) = o match {
    case Some(s: DBObject) => Some(new ServerStatus(s.get("_id").asInstanceOf[ObjectId], s.get("server_id").asInstanceOf[ObjectId],
        s.get("date").asInstanceOf[Date], s.get("online").asInstanceOf[Boolean], s.get("info").asInstanceOf[String]))
    case _ => None
  }
  
  def getStatus(id: ObjectId) = mapToStatus(getOne(MongoDBObject("_id" -> id)))
  
  def getStatusList(o: DBObject) = get(o).map{obj: DBObject => mapToStatus(Some(obj)).get}
  
}