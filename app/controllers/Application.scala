package controllers

import play.api._
import play.api.mvc._
import models._
import twitter4j.auth.AccessToken
import twitter4j.User

/**
 * アプリケーションコントローラ
 */
object Application extends Controller {

  val twitterModel: TwitterModel = new TwitterModel
  def index(pageNum: Int) = Action { implicit request =>
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("とっぷ", pageNum, 0, null, Seq.empty)
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId)
      case _ => null
    }
    Ok(views.html.index(pager, twitterUser))
  }

  /**
   * Twitterへログインする
   */
  def twitterLogin = Action { implicit request =>
    // Twitter Object初期化
    twitterModel.getNewTwitter
    Redirect(twitterModel.getOAuthUrl())
  }

  /**
   * TwitterからのCallBack処理
   */
  def twitterOAuthCallback = Action { implicit request =>

    println(" ■ ------------- " + request)
    println(" ■ ------------- " + request.queryString)
    println(" ■ ------------ oauth_token- " + request.queryString.get("oauth_token"))
    println(" ■ ------------ oauth_verifier- " + request.queryString.get("oauth_verifier"))

    request.queryString.get("denied").map {

      // SessionからTwitterIdを削除
      Redirect(routes.Application.index(1)).withSession(session - "twitterId")

    }.getOrElse {

      // AuthTokenを取得する
      var authToken: String = request.queryString.get("oauth_token").head
      var authVerifier: String = request.queryString.get("oauth_verifier")head

      // OAuthトークンの設定
      twitterModel.accessToken = twitterModel.getTwitter.getOAuthAccessToken(twitterModel.requestToken, authVerifier)
      // Twitterオブジェクトの認証
      twitterModel.getTwitter.verifyCredentials()

      // TwitterのUserオブジェクトを取得
      var user: User = twitterModel.getTwitter.showUser(twitterModel.getTwitter.getId())

      // TwitterUserが取得出来た場合はそのまま利用する
      def nowDate: java.util.Date = new java.util.Date
      TwitterUser.getByTwitterId(String.valueOf(twitterModel.getTwitter.getId())) match {
        case Some(s) => {
          // 既にログインした事有る場合はUpdateを行う
          val tmp: TwitterUser = new TwitterUser(s.id
                                                ,twitterModel.getTwitter.getId()
                                                ,twitterModel.getTwitter.getScreenName()
                                                ,user.getProfileImageURL()
                                                ,twitterModel.accessToken.getToken
                                                ,twitterModel.accessToken.getTokenSecret
                                                ,Some(nowDate)
                                                ,Some(nowDate))
          TwitterUser.update(tmp)
          Option(tmp)
        }
        case _ => {
          // ログインしたらtwitter_userに登録
          val tmp: TwitterUser = new TwitterUser(null
                                                ,twitterModel.getTwitter.getId()
                                                ,twitterModel.getTwitter.getScreenName()
                                                ,user.getProfileImageURL()
                                                ,twitterModel.accessToken.getToken
                                                ,twitterModel.accessToken.getTokenSecret
                                                ,Some(nowDate)
                                                ,Some(nowDate))
          TwitterUser.insart(tmp)
          Option(tmp)
        }
      }
      // CookieにTwitterIdを保存
      Redirect(routes.Application.index(1)).withSession("twitterId" -> String.valueOf(twitterModel.getTwitter.getId()))
    }
  }

  def twitterLogout = Action { implicit request =>
    twitterModel.shutdown
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("とっぷ", 1, 0, null, Seq.empty)
    // SessionからTwitterIdを削除
    Redirect(routes.Application.index(1)).withSession(session - "twitterId")
  }
}
