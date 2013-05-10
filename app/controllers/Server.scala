package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import controllers.security.Secured
import play.api.i18n.Messages
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import java.util.Calendar
import play.api.i18n.Lang

object Server extends Controller with Secured {
  
  implicit val defaultLang = Lang("pl")

  def createForm(implicit req: Request[AnyRef]) = Form( tuple(
    "ip" -> text.verifying(Messages("validation.required")(language), !_.isEmpty).verifying(Messages("serv.badIP")(language), 
        ip => ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")),
    "name" -> text.verifying(Messages("validation.required")(language), !_.isEmpty),
    "location" -> text.verifying(Messages("validation.required")(language), !_.isEmpty).verifying(Messages("serv.letdig")(language),
        loc => loc.matches("[\\w\\s\\d]+")),
    "details" -> text,
    "method" -> text.verifying(Messages("validation.required")(language), !_.isEmpty).verifying(Messages("serv.invalidMethod")(language), m => model.Server.checkMethods.get(m) != None),
    "period" -> text.verifying(Messages("validation.number")(language), _.matches("[0-9]+"))
    ))
    
  def isOwner(editedContet: Option[model.Server]) = { user: model.User =>
     user._id == editedContet.get.user_id
  }
    
  def create = withUser(model.User.roles.get("regular").get) { user => implicit request => 
    Ok(views.html.server.create(createForm, user, language))
  }

  def submit = withUser(model.User.roles.get("regular").get) { user => implicit request => 
    createForm.bindFromRequest.fold(
      errors => BadRequest(views.html.server.create(errors, user, language)),
      obj => {
        val id = model.Server.createServer(obj._1, obj._2, obj._3, obj._4, user.get._id, obj._5, obj._6.toInt)
        Redirect(routes.Server.view(id.toString())).flashing("success" -> Messages("serv.created")(language))
      })
  }
  
  def index = withUser(model.User.roles.get("viewer").get) { user => implicit request => 
    Ok(views.html.server.index(model.Server.getAllServers, user, language))
  }
  
  def edit(id: String) = withUser(model.User.roles.get("admin").get, isOwner(model.Server.getServer(new ObjectId(id)))) { user => implicit request => 
    model.Server.getServer(new ObjectId(id)) match {
      case Some(s: model.Server) => Ok(views.html.server.edit(createForm.fill(s.ip, s.name, s.location, s.details, s.method, s.period.toString()),
          s, user, language, user.get.role == "admin"))
      case _ => Redirect(routes.Application.index).flashing("error" -> Messages("app.notFound")(language))
    }
  }
  
  def update(id: String) = withUser(model.User.roles.get("admin").get, isOwner(model.Server.getServer(new ObjectId(id)))) { user => implicit request => 
    createForm.bindFromRequest.fold(
      errors => {
        val oldServer = model.Server.getServer(new ObjectId(id)).get
        BadRequest(views.html.server.edit(errors, oldServer, user, language, user.get.role == "admin"))
      },
      obj => {
        val oldServer = model.Server.getServer(new ObjectId(id))
        model.Server.saveServer(new model.Server(new ObjectId(id), obj._1, obj._2, obj._3, obj._4, oldServer.get.user_id, obj._5, obj._6.toInt))
        Redirect(routes.Server.index).flashing("success" -> Messages("serv.updated")(language))
      })
  }
  
  def delete(id: String) = withUser(model.User.roles.get("admin").get, isOwner(model.Server.getServer(new ObjectId(id)))) { user => implicit request => {
    model.Server.delete(new ObjectId(id))
    Redirect(routes.Server.index).flashing("success" -> Messages("serv.deleted")(language))
  }}
  
  def view(id: String) = withUser(model.User.roles.get("viewer").get) { user => implicit request => 
    model.Server.getServer(new ObjectId(id)) match {
      case Some(s: model.Server) => {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        val q: DBObject = ("date" $gte cal.getTime()) ++ ("server_id" -> s._id)
        val arrChart = new JsArray(model.ServerStatus.getStatusList(q, MongoDBObject("date" -> 1))
            .map{s => new JsObject(Seq(("date", Json.toJson(s.date)), ("online", Json.toJson(s.online))))})
        
        cal.add(Calendar.DAY_OF_YEAR, -24)
        val aggr1 = new MongoDBList()
        aggr1 += MongoDBObject("$match" -> (("date" $gte cal.getTime()) ++ ("server_id" -> s._id)))
        aggr1 += MongoDBObject("$group" -> MongoDBObject(
          "_id" -> "$online",
          "count" -> MongoDBObject("$sum" -> 1)
          ))
        aggr1 += MongoDBObject("$sort" -> MongoDBObject("_id" -> 1))
        val res = model.Server.aggregate(model.ServerStatus.entityName, aggr1)

        Ok(views.html.server.view(s, user, arrChart, res.asInstanceOf[BasicDBList], language))
      }
      case _ => Redirect(routes.Application.index).flashing("error" -> Messages("app.notFound")(language))
    }
  }
}