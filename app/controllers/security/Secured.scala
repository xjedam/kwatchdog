package controllers.security

import play.api.mvc._
import controllers.routes

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Forbidden

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withUser(accessLevel: Int)(f: model.User => Request[AnyContent] => Result) = withAuth { username =>
    implicit request =>
      model.User.getUser(username).map { user =>
        if (model.User.roles.get(user.role).getOrElse(-1) >= accessLevel) {
          f(user)(request)
        } else {
          onUnauthorized(request)
        }
      }.getOrElse(onUnauthorized(request))
  }
}