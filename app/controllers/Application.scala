package controllers

import play.api._
import play.api.mvc._
import controllers.security.Secured
import play.api.i18n.Messages

object Application extends Controller with Secured{

  def index = Action { implicit request =>
    Ok(views.html.index(Messages("app.greeting")(language), model.User.getUser(request.session.get(Security.username).getOrElse("")), language))
  }
}