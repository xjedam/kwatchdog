package model

import com.mongodb.casbah.Imports._
import play.api.Play
import com.mongodb.WriteConcern
import scala.collection.immutable.Map
import com.mongodb.casbah.commons.MongoDBObject

abstract class Entity(collection: String) {
  val mongoClient = MongoClient(Play.current.configuration.getString("mongohost").getOrElse(null),
    Play.current.configuration.getString("mongoport").getOrElse(null).toInt)(Play.current.configuration.getString("mongodb").getOrElse(null))
  val coll = mongoClient(collection)

  def save(entity: DBObject) = {
    coll.save(entity, WriteConcern.NORMAL)
  }

  def getOne(searchObject: DBObject) = {
    coll.findOne(searchObject)
  }
  
  def getAll = coll.find(MongoDBObject.empty).toList
}