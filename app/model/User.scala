package model

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import scala.collection.immutable.Map

case class User(
  _id: ObjectId,
  login: String,
  role: String,
  lang: String)

object User extends Entity("users") {

  val roles = Map("viewer" -> 5, "regular" -> 15, "admin" -> 30)

  def mapToUser(o: Option[DBObject]) = o match {
    case Some(u: DBObject) => Some(new User(u.get("_id").asInstanceOf[ObjectId], u.get("login").asInstanceOf[String], u.get("role").asInstanceOf[String],
        u.get("lang").asInstanceOf[String]))
    case _ => None
  }

  def createUser(login: String, role: String = "regular", lang: String) = {
    save(MongoDBObject("login" -> login, "role" -> role, "lang" -> lang))
  }

  def getUser(login: String): Option[model.User] = {
    mapToUser(getOne(MongoDBObject("login" -> login)))
  }
  
  def getAllUsers = getAll.map{obj: DBObject => mapToUser(Some(obj)).get}
  
  def saveUser(user: model.User) = save(MongoDBObject("login"-> user.login, "_id" -> user._id, "role" -> user.role, "lang" -> user.lang))
}

