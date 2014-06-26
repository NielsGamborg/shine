package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import views._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Shine Application"))
  }

  // -- Authentication

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
      case (email, password) => Account.authenticate(email, password).isDefined
    })
  )

  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.login(loginForm, "Shine Application"))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors, "Shine Application")),
      account => Redirect(routes.Application.index).withSession("email" -> account._1)
    )
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

  // -- Javascript routing

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Search.suggestTitle,
        routes.javascript.Search.suggestUrl,
        routes.javascript.Search.suggestFileFormat,
        routes.javascript.Search.suggestLinksHosts,
        routes.javascript.Search.suggestLinksDomains,
        routes.javascript.Search.suggestLinksPublicSuffixes,
        routes.javascript.Search.suggestAuthor,
        routes.javascript.Search.suggestCollection,
        routes.javascript.Search.suggestCollections,
        routes.javascript.Search.getFacets,
        routes.javascript.Search.processChart,
        routes.javascript.Search.ajaxSearch
        )).as("text/javascript")
  }
}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }
}
