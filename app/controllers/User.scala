package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.Form
import play.api.data.Forms._

object User extends Controller {
  val createForm = Form(
    "login" -> nonEmptyText)
  def create = Action {
    Ok(views.html.user.create(createForm))
  }

  def submit = Action { implicit request =>
    createForm.bindFromRequest.fold(
      errors => BadRequest(":("),
      obj => {
        model.User.createUser(obj)
        Redirect(routes.Auth.login)
      })
  }
}