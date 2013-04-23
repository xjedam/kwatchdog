package controllers.security

import play.api.mvc._
import controllers.routes
import play.api.Play
import play.api.i18n.Lang

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Forbidden

  implicit def language(implicit request: RequestHeader) = {
    play.api.Play.maybeApplication.map { implicit app =>
      request.session.get(Security.username) match {
        case Some(uname: String) => model.User.getUser(uname) match {
          case Some(user: model.User) => Lang(user.lang)
          case _ => {
            val maybeLangFromCookie = request.cookies.get(Play.langCookieName).flatMap(c => Lang.get(c.value))
            maybeLangFromCookie.getOrElse(play.api.i18n.Lang.preferred(request.acceptLanguages))
          }
        }
        case _ => {
          val maybeLangFromCookie = request.cookies.get(Play.langCookieName).flatMap(c => Lang.get(c.value))
          maybeLangFromCookie.getOrElse(play.api.i18n.Lang.preferred(request.acceptLanguages))
        }
      }
      
    }.getOrElse(request.acceptLanguages.headOption.getOrElse(play.api.i18n.Lang.defaultLang))
  }

  def withAuth(accessLevel: Int)(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withUser(accessLevel: Int)(f: Option[model.User] => Request[AnyContent] => Result) = withAuth(accessLevel) { username =>
    implicit request =>
      model.User.getUser(username).map { user =>
        if (model.User.roles.get(user.role).getOrElse(-1) >= accessLevel) {
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