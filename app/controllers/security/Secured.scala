package controllers.security

import play.api.mvc._
import play.api.Application
import controllers.routes
import play.api.Play
import play.api.i18n.Lang
import play.api.Play.current

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Forbidden
  
  def defLang(implicit request: RequestHeader) = {
    val maybeLangFromCookie = request.cookies.get(Play.langCookieName).flatMap(c => Lang.get(c.value))
    maybeLangFromCookie.getOrElse(play.api.i18n.Lang.preferred(request.acceptLanguages))
  }

  implicit def language(implicit request: RequestHeader) = {
    play.api.Play.maybeApplication.map { implicit app =>
      request.session.get(Security.username) match {
        case Some(uname: String) => model.User.getUser(uname) match {
          case Some(user: model.User) => if(user.lang.length == 2) 
            Lang(user.lang) 
          else 
            defLang
          case _ => defLang
        }
        case _ => defLang
      }
    }.getOrElse(request.acceptLanguages.headOption.getOrElse(play.api.i18n.Lang.defaultLang))
  }
  
  def optionalAuth(f: Option[model.User] => Request[AnyContent] => Result) = Action { implicit request =>
    f(model.User.getUser(request.session.get(Security.username).getOrElse("")))(request)
  }
  
  def withAuth(accessLevel: Int)(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withUser(accessLevel: Int, otherPermissions: (model.User => Boolean) = u => false)(f: Option[model.User] => Request[AnyContent] => Result) = withAuth(accessLevel) { username =>
    implicit request =>
      model.User.getUser(username).map { user =>
        if (model.User.roles.get(user.role).getOrElse(-1) >= accessLevel || otherPermissions(user)) {
          f(Some(user))(request)
        } else {
          onUnauthorized(request)
        }
      }.getOrElse({
        if (accessLevel <= 5) {
          f(None)(request)
        } else {
          onUnauthorized(request)
        }
      })
  }
}