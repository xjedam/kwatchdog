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
        log => log.matches("[@.\\w\\d]+")).verifying(Messages("auth.notAdmin")(language), _ => model.User.isAdmin(req.session.get(Security.username).get)),
    "role" -> nonEmptyText.verifying(Messages("auth.loginVerify")(language),
        role => model.User.roles.keySet.contains(role)).verifying(Messages("auth.notAdmin")(language), _ => model.User.isAdmin(req.session.get(Security.username).get)),
    "lang" -> nonEmptyText(2, 2),
    "disabled" -> boolean.verifying(Messages("auth.notAdmin")(language), _ => model.User.isAdmin(req.session.get(Security.username).get))
        ))
    
  def create = Action { implicit request =>
    Ok(views.html.user.create(createForm, language))
  }
  
  def isOwner(editedContet: Option[model.User]) = { user: model.User =>
     user._id == editedContet.get._id
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
  
  def update(uid: String) = withUser(30, isOwner(model.User.getUser(new ObjectId(uid)))) { user => implicit request => {
    editForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson(language)),
      obj => {
        model.User.saveUser(new model.User(new ObjectId(uid), obj._1, obj._2, obj._3, obj._4))
        Redirect(routes.User.view(obj._1))
      })
  }}
  
  def edit(username: String) = withUser(30, isOwner(model.User.getUser(username))) { user => implicit request => {
    model.User.getUser(username) match {
      case Some(u: model.User) => Ok(views.html.user.edit(editForm, u, user, language, user.get.role == "admin"))
      case _ => BadRequest(":(")
    }
  }}
  
  def delete(username: String) = withUser(30) { user => implicit request => {
    model.User.getUser(username) match {
      case Some(u: model.User) => {
        model.User.delete(u._id)
        Redirect(routes.User.index)
      }
      case _ => BadRequest(":(")
    }
  }}
}