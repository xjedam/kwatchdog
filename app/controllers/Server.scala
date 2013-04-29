package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.Form
import play.api.data.Forms._
import controllers.security.Secured
import play.api.i18n.Messages
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.Json

object Server extends Controller with Secured {
  def createForm(implicit req: Request[AnyContent]) = Form( tuple(
    "ip" -> nonEmptyText.verifying(Messages("serv.badIP")(language), 
        ip => ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")),
    "name" -> nonEmptyText,
    "location" -> nonEmptyText.verifying(Messages("serv.latdig")(language),
        loc => loc.matches("[\\w\\s\\d]+")),
    "details" -> text,
    "method" -> nonEmptyText.verifying(Messages("serv.invalidMethod")(language), m => model.Server.checkMethods.get(m) != None),
    "period" -> number
    ))
    
  def isOwner(editedContet: Option[model.Server]) = { user: model.User =>
     user._id == editedContet.get.user_id
  }
    
  def create = withUser(15) { user => implicit request => 
    Ok(views.html.server.create(createForm(request), user, language))
  }

  def submit = withUser(15) { user => implicit request => 
    createForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson(language)),
      obj => {
        model.Server.createServer(obj._1, obj._2, obj._3, obj._4, user.get._id, obj._5, obj._6)
        Ok(views.html.index(Messages("serv.created")(language), None, language))
      })
  }
  
  def index = withUser(15) { user => implicit request => 
    Ok(views.html.server.index(model.Server.getUsersServer(user.get._id), user, language))
  }
  
  def edit(id: String) = withUser(30, isOwner(model.Server.getServer(new ObjectId(id)))) { user => implicit request => 
    model.Server.getServer(new ObjectId(id)) match {
      case Some(s: model.Server) => Ok(views.html.server.edit(createForm.fill(s.ip, s.name, s.location, s.details, s.method, s.period),
          s, user, language, user.get.role == "admin"))
      case _ => BadRequest(":(")
    }
  }
  
  def update(id: String) = withUser(30, isOwner(model.Server.getServer(new ObjectId(id)))) { user => implicit request => 
    createForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson(language)),
      obj => {
        val oldServer = model.Server.getServer(new ObjectId(id))
        model.Server.saveServer(new model.Server(new ObjectId(id), obj._1, obj._2, obj._3, obj._4, oldServer.get.user_id, obj._5, obj._6))
        Redirect(routes.Server.index)
      })
  }
  
  def delete(id: String) = withUser(30, isOwner(model.Server.getServer(new ObjectId(id)))) { user => implicit request => {
    model.Server.delete(new ObjectId(id))
    Redirect(routes.Server.index)
  }}
  
  def view(id: String) = withUser(30, isOwner(model.Server.getServer(new ObjectId(id)))) { user => implicit request => 
    model.Server.getServer(new ObjectId(id)) match {
      case Some(s: model.Server) => {
        val arr = new JsArray(model.ServerStatus.getStatusList(MongoDBObject("server_id" -> s._id))
            .map{s => new JsObject(Seq(("date", Json.toJson(s.date)), ("online", Json.toJson(s.online))))})
        Ok(views.html.server.view(s, user, arr, language))
      }
      case _ => BadRequest(":(")
    }
  }
}