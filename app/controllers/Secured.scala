package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import anorm._
import models._
import views._

/**
 * Provide security features
 */
trait Secured {
  /** Retrieve the connected twitterId. */
  private def twitterId(request: RequestHeader) = request.session.get("twitterId")
  /** Redirect to login if the twitterId in not authorized. */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.TwitterController.twitterLogin)
  /** Action for authenticated users. */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(twitterId, onUnauthorized) { twitterId =>
  	Action { request => 
	  	request.session.get("accessToken") match {
	  		case Some(accessToken) => {
				TwitterUser.checkExistsTwitterUser(twitterId.toLong, accessToken, accessTokenSecret) match {
			  		case Some(twitterUser) => f(twitterId)(request)
			  		// twitter_id、access_tokenでTwitterUserが取得できない場合、認証へ
			  		case _                 => Results.Redirect(routes.TwitterController.twitterLogin)
			  	}
	  		}
	  		// Access_Tokenが存在しない場合、認証へ
	  		case _ => Results.Redirect(routes.TwitterController.twitterLogin)
	  	}
  	}
  }
}
