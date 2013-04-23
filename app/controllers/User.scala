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

object User extends Controller with Secured {
  def createForm(implicit req: Request[AnyContent]) = Form(
    "login" -> nonEmptyText.verifying(Messages("auth.loginVerify")(language),
        log => log.matches("[@.\\w\\d]+")))
    
  def editForm(implicit req: Request[AnyContent]) = Form( tuple(
    "login" -> nonEmptyText.verifying(Messages("auth.loginVerify")(language),
        log => log.matches("[@.\\w\\d]+")),
    "role" -> nonEmptyText.verifying(Messages("auth.loginVerify")(language),
        role => model.User.roles.keySet.contains(role)),
    "lang" -> nonEmptyText(2, 2)
        ))
    
  def create = Action { implicit request =>
    Ok(views.html.user.create(createForm, language))
  }

  def submit = Action { implicit request =>
    createForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson(language)),
      obj => {
        model.User.createUser(obj, "regular", language.country)
        Redirect(routes.Auth.login)
      })
  }
  
  def index = withUser(30) { user => implicit request => {
    Ok(views.html.user.index(model.User.getAllUsers, user, language))
  }}
  
  def view(username: String) = withUser(15) { user => implicit request => {
    model.User.getUser(username) match {
      case Some(u: model.User) => Ok(views.html.user.view(u, user, language))
      case _ => BadRequest(":(")
    }
  }}
  
  def update(uid: String) = withUser(30) { user => implicit request => {
    editForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson(language)),
      obj => {
        model.User.saveUser(new model.User(new ObjectId(uid), obj._1, obj._2, obj._3))
        Redirect(routes.User.view(obj._1))
      })
  }}
  
  def edit(username: String) = withUser(30) { user => implicit request => {
    model.User.getUser(username) match {
      case Some(u: model.User) => Ok(views.html.user.edit(editForm, u, user, language))
      case _ => BadRequest(":(")
    }
  }}
  
  def delete(username: String) = withUser(30) { user => implicit request => {
    model.User.getUser(username) match {
      case Some(u: model.User) => Ok(views.html.user.delete(u, user, language))
      case _ => BadRequest(":(")
    }
  }}
}