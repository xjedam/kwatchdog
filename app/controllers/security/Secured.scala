package controllers.security

import play.api.mvc._
import controllers.routes

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  /**
   * This method shows how you could wrap the withAuth method to also fetch your user
   * You will need to implement UserDAO.findOneByUsername
   */
  def withUser(f: model.User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    model.User.getUser(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }
}