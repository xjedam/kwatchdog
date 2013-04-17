package model

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject

case class User (
	login: String
)

object User extends Entity[User]("users") {
  def createUser(login: String) = {
    save(MongoDBObject("login" -> login))
  }
}