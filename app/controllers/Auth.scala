package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import com.mongodb.DBObject
import play.api.i18n.Messages
import controllers.security.Secured

object Auth extends Controller with Secured {

  def loginForm(implicit req: Request[AnyContent]) = Form(
    tuple(
      "login" -> text,
      "password" -> text)
      verifying (Messages("auth.badpass")(language), result => result match {
        case (login, password) => check(login, password)
      })
      verifying(Messages("auth.accDisabled")(language), result => result match {
        case (login, password) => checkDisabled(login)
      })
      )

  def check(username: String, password: String) = {
    val user = model.User.getUser(username)
    val test = user match {
      case Some(c: model.User) => c.login.endsWith("@kainos.com")
      case _ => false
    }
    password == globalPass && test
  }
  
  def checkDisabled(username: String) = {
    val user = model.User.getUser(username)
    user match {
      case Some(c: model.User) => !c.disabled
      case _ => false
    }
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm(request), language))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors, language)),
      user => Redirect(routes.Application.index).withSession(Security.username -> user._1))
  }

  def logout = Action { implicit request =>
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> Messages("auth.loggedout")(language))
  }
}