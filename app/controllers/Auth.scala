package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import com.mongodb.DBObject

object Auth extends Controller {

  private val globalPass = "qwerasdf"

  val loginForm = Form(
    tuple(
      "login" -> text,
      "password" -> text)
      verifying ("Invalid login or password", result => result match {
        case (login, password) => check(login, password)
      }))

  def check(username: String, password: String) = {
    val user = model.User.getUser(username)
    val test = user match {
      case Some(c: model.User) => c.login.endsWith("@kainos.com")
      case _ => false
    }
    password == globalPass && test
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(":("),
      user => Redirect(routes.Application.index).withSession(Security.username -> user._1))
  }

  def logout = Action {
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> "You are now logged out.")
  }
}