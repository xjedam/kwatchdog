package model

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId

case class User (
    _id: ObjectId,
	login: String
)

object User extends Entity("users") {
  
  def createUser(login: String) = {
    save(MongoDBObject("login" -> login))
  }
  
  def getUser(login: String) = {
    getOne(MongoDBObject("login" -> login)) match {
	  case Some(u: DBObject) => Some(new User(u.get("_id").asInstanceOf[ObjectId], u.get("login").asInstanceOf[String]))
	  case _ => None
	}
  }
}