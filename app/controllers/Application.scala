package controllers

import play.api._
import play.api.mvc._
import controllers.security.Secured
import play.api.i18n.Messages

object Application extends Controller with Secured{

  def index = Action { implicit request =>
    request.session.get(Security.username) match {
      case Some(uname:String) => Ok(views.html.index(Messages("app.greeting")(language), uname, language))
      case _ => Ok(views.html.index(Messages("app.greeting")(language), "", language))
    }
  }
}