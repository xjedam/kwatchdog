package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.Form
import play.api.data.Forms._
import controllers.security.Secured

object User extends Controller with Secured {
  val createForm = Form(
    "login" -> nonEmptyText)
  def create = Action { implicit request =>
    Ok(views.html.user.create(createForm, language))
  }

  def submit = Action { implicit request =>
    createForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson(language)),
      obj => {
        model.User.createUser(obj)
        Redirect(routes.Auth.login)
      })
  }
}