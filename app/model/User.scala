package model

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import scala.collection.immutable.Map

case class User(
  _id: ObjectId,
  login: String,
  role: String
  )

object User extends Entity("users") {

  val roles = Map("viewer" -> 5, "regular" -> 15, "admin" -> 30)
  
  def createUser(login: String, role: String = "regular") = {
    save(MongoDBObject("login" -> login, "role" -> role))
  }

  def getUser(login: String) = {
    getOne(MongoDBObject("login" -> login)) match {
      case Some(u: DBObject) => Some(new User(u.get("_id").asInstanceOf[ObjectId], u.get("login").asInstanceOf[String], u.get("role").asInstanceOf[String]))
      case _ => None
    }
  }
}

