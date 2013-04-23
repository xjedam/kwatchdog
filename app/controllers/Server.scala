package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.Form
import play.api.data.Forms._
import controllers.security.Secured
import play.api.i18n.Messages

object Server extends Controller with Secured {
  def createForm(implicit req: Request[AnyContent]) = Form( tuple(
    "ip" -> nonEmptyText.verifying(Messages("serv.badIP")(language), 
        ip => ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")),
    "name" -> nonEmptyText,
    "location" -> nonEmptyText.verifying(Messages("serv.latdig")(language),
        loc => loc.matches("[\\w\\s\\d]+")),
    "details" -> text
    ))
    
    
  def create = withUser(15) { user => implicit request => {
    Ok(views.html.server.create(createForm(request), user, language))
  }}

  def submit = withUser(15) { user => implicit request => 
    createForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson(language)),
      obj => {
        model.Server.createServer(obj._1, obj._2, obj._3, obj._4, user.get._id)
        Ok(views.html.index(Messages("serv.created")(language), None, language))
      })
  }
}