package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.Form
import play.api.data.Forms._
import controllers.security.Secured

object Server extends Controller with Secured {
  val createForm = Form( tuple(
    "ip" -> nonEmptyText.verifying("Must be a proper IP address", 
        ip => ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")),
    "name" -> nonEmptyText,
    "location" -> nonEmptyText.verifying("Can contain only letters or digits",
        loc => loc.matches("[\\w\\s\\d]+")),
    "details" -> text
    ))
    
    
  def create = withUser(15) { user => implicit request =>
    Ok(views.html.server.create(createForm, user.login))
  }

  def submit = withUser(15) { user => implicit request => 
    createForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson),
      obj => {
        model.Server.createServer(obj._1, obj._2, obj._3, obj._4, user._id)
        Ok(views.html.index("Smth happened", ""))
      })
  }
}