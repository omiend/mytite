package controllers

import play.api._
import play.api.mvc._
import models._

/**
 * アプリケーションコントローラ
 */
object Application extends Controller with Secured {

  /**
   * トップページ
   */
  def index(pageNum: Int) = Action { implicit request =>

    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する（直接Cookieから取得するのはここだけで、あとはIsAuthenticatedを使う）
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId)
      case _ => null
    }

    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("とっぷ", pageNum, 0, twitterUser, Seq.empty)

    // TwitterUserのリストを取得
    val resultTuple = TwitterUser.findFromTo(pager.pageNum * pager.maxListCount - pager.maxListCount, pager.maxListCount)

    // データリスト
    pager.dataList = resultTuple._1

    // 全体件数
    pager.totalRows = resultTuple._2.toInt

    Ok(views.html.index(pager))
  }

  /**
   * フェス一覧画面起動
   */
  def createFestival(twitterId: Long) = IsAuthenticated { twitterId => implicit request =>

    // IsAuthenticatedからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get(twitterId) match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId)
      case _ => null
    }

    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("とっぷ", 1, 0, twitterUser, Seq.empty)
    Ok(views.html.festivalIndex(pager))
  }
}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected twitterId email.
   */
  private def twitterId(request: RequestHeader) = request.session.get("twitterId")

  /**
   * Redirect to login if the twitterId in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.TwitterController.twitterLogin)
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(twitterId, onUnauthorized) { twitterId =>
    Action(request => f(twitterId)(request))
  }

}
