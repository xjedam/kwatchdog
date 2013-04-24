package model

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import scala.collection.immutable.Map

case class User(
  _id: ObjectId,
  login: String,
  role: String,
  lang: String,
  disabled: Boolean)

object User extends Entity("users") {

  val roles = Map("viewer" -> 5, "regular" -> 15, "admin" -> 30)
  
  def isAdmin(uname: String) = {
    getUser(uname) match {
      case Some(u: model.User) => u.role == "admin"
      case _ => false
    }
  }

  def mapToUser(o: Option[DBObject]) = o match {
    case Some(u: DBObject) => Some(new User(u.get("_id").asInstanceOf[ObjectId], u.get("login").asInstanceOf[String], u.get("role").asInstanceOf[String],
        u.get("lang").asInstanceOf[String], u.get("disabled").asInstanceOf[Boolean]))
    case _ => None
  }

  def createUser(login: String, role: String = "regular", lang: String, disabled: Boolean = false) = {
    save(MongoDBObject("login" -> login, "role" -> role, "lang" -> lang, "disabled" -> disabled))
  }

  def getUser(login: String): Option[model.User] = {
    mapToUser(getOne(MongoDBObject("login" -> login)))
  }
  
  def getUser(uid: ObjectId): Option[model.User] = {
    mapToUser(getOne(MongoDBObject("_id" -> uid)))
  }
  
  def getAllUsers = getAll.map{obj: DBObject => mapToUser(Some(obj)).get}
  
  def saveUser(user: model.User) = save(MongoDBObject("login"-> user.login, "_id" -> user._id, "role" -> user.role, "lang" -> user.lang, "disabled" -> user.disabled))
}

