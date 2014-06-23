package controllers

import play.api._, mvc._

/**
 * Ajax用 javascriptRouter
 */
object JsRouter extends Controller {
  
  def javascriptRoutes = Action { implicit request =>
      import routes.javascript._
      Ok(
        Routes.javascriptRouter("jsRoutes")(
           routes.javascript.AjaxController.ajaxUpdateFestival
          ,routes.javascript.AjaxController.ajaxUpdateStage
          ,routes.javascript.AjaxController.ajaxUpdatePerformance
          ,routes.javascript.AjaxController.ajaxUpdatePerformanceByTimeFrame
          ,routes.javascript.AjaxController.ajaxInsertHeart
          ,routes.javascript.AjaxController.ajaxDeleteHeart
        )
      ).as("text/javascript")
  }
}
