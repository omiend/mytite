package controllers

import play.api._
import play.api.mvc._
import models._
import twitter4j._
import twitter4j.auth._
import twitter4j.auth.AccessToken
import twitter4j.User

/**
 * ツイッターログインコントローラ
 */
object TwitterController extends Controller {

  var twitter: Twitter = null
  var requestToken: RequestToken = null
  var accessToken: AccessToken = null

  /**
   * Twitterへログインする
   */
  def twitterLogin = Action { implicit request =>
    // Twitterオブジェクトの初期化
    twitter = (new TwitterFactory()).getInstance()
    // RequestTokenの取得
    requestToken = twitter.getOAuthRequestToken("http://" + request.host + "/twitterOAuthCallback")
    // Twitterのログイン画面にリダイレクト
    Redirect(requestToken.getAuthorizationURL())
  }

  /**
   * TwitterからのCallBack処理
   */
  def twitterOAuthCallback = Action { implicit request =>

    request.queryString.get("denied") match {
      // Twitterのアプリケーション承認キャンセル時
      case Some(denied) => Redirect(routes.TwitterController.twitterLogout)
      // Twitterのアプリケーション承認時
      case _ => {

        // AuthTokenを取得する
        var authToken   : String = request.queryString.get("oauth_token").get.head
        var authVerifier: String = request.queryString.get("oauth_verifier").get.head

        // OAuthトークンの設定
        accessToken = twitter.getOAuthAccessToken(requestToken, authVerifier)

        // Twitterオブジェクトの認証
        twitter.verifyCredentials()

        // TwitterのUserオブジェクトを取得
        var user: User = twitter.showUser(twitter.getId())

        // TwitterUserが取得出来た場合はそのまま利用する
        def nowDate: java.util.Date = new java.util.Date
        TwitterUser.getByTwitterId(twitter.getId()) match {
          case Some(s) => {
            // 既にログインした事有る場合はUpdateを行う
            val tmp: TwitterUser = new TwitterUser(s.id
                                                  ,twitter.getId()
                                                  ,user.getName()
                                                  ,twitter.getScreenName()
                                                  ,user.getProfileImageURL()
                                                  ,user.getDescription()
                                                  ,accessToken.getToken
                                                  ,accessToken.getTokenSecret
                                                  ,Some(nowDate)
                                                  ,Some(nowDate))
            TwitterUser.update(tmp) // Coution! Option.get
            Option(tmp)
          }
          case _ => {
            // ログインしたらtwitter_userに登録
            val tmp: TwitterUser = new TwitterUser(None
                                                  ,twitter.getId()
                                                  ,user.getName()
                                                  ,twitter.getScreenName()
                                                  ,user.getProfileImageURL()
                                                  ,user.getDescription()
                                                  ,accessToken.getToken
                                                  ,accessToken.getTokenSecret
                                                  ,Some(nowDate)
                                                  ,Some(nowDate))
            TwitterUser.insart(tmp)
            Option(tmp)
          }
        }
        // CookieにTwitterId等を保存し、return
        Redirect(routes.Application.index(1)).withSession(
           "twitterId"         -> String.valueOf(twitter.getId())
          ,"accessToken"       -> accessToken.getToken
          ,"accessTokenSecret" -> accessToken.getTokenSecret
        )
      }
    }
  }

  /** 
   * ログアウト処理
   */
  def twitterLogout = Action { implicit request =>
    // Twitter ShutDown
    if (twitter != null) {
      twitter.shutdown()
      twitter = null
      requestToken = null
      accessToken = null
    }
    // Pager初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("とっぷ", 1, 0, null, Seq.empty)
    // SessionからTwitterIdを削除
    Redirect(routes.Application.index(1)).withSession(
      request.session - "twitterId" 
                      - "accessToken" 
                      - "accessTokenSecret"
    )
  }
}
