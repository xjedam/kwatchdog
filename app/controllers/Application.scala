package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action { request =>
    Ok(views.html.index("Your new application is ready.", request.session.get(Security.username).getOrElse("")))
  }

}