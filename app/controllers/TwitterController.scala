package controllers

import play.api._
import play.api.mvc._
import models._
import twitter4j.auth.AccessToken
import twitter4j.User

/**
 * ツイッターログインコントローラ
 */
object TwitterController extends Controller {

  val twitterModel: TwitterModel = new TwitterModel

  /**
   * Twitterへログインする
   */
  def twitterLogin = Action { implicit request =>

    // Twitter Object初期化
    twitterModel.getNewTwitter

    // Twitterのログイン画面にリダイレクト
    Redirect(twitterModel.getOAuthUrl())
  }

  /**
   * TwitterからのCallBack処理
   */
  def twitterOAuthCallback = Action { implicit request =>

    request.queryString.get("denied") match {
      // Twitterのアプリケーション承認キャンセル時
      case Some(denied) => {
        // SessionからTwitterIdを削除し、return
        Redirect(routes.Application.index(1)).withSession(request.session - "twitterId")
      }
      // Twitterのアプリケーション承認時
      case _ => {

        // AuthTokenを取得する
        var authToken   : String = request.queryString.get("oauth_token").get.head
        var authVerifier: String = request.queryString.get("oauth_verifier").get.head

        // OAuthトークンの設定
        twitterModel.accessToken = twitterModel.getTwitter.getOAuthAccessToken(twitterModel.requestToken, authVerifier)

        // Twitterオブジェクトの認証
        twitterModel.getTwitter.verifyCredentials()

        // TwitterのUserオブジェクトを取得
        var user: User = twitterModel.getTwitter.showUser(twitterModel.getTwitter.getId())

        // TwitterUserが取得出来た場合はそのまま利用する
        def nowDate: java.util.Date = new java.util.Date
        TwitterUser.getByTwitterId(twitterModel.getTwitter.getId()) match {
          case Some(s) => {
            // 既にログインした事有る場合はUpdateを行う
            val tmp: TwitterUser = new TwitterUser(s.id
                                                  ,twitterModel.getTwitter.getId()
                                                  ,user.getName()
                                                  ,twitterModel.getTwitter.getScreenName()
                                                  ,user.getProfileImageURL()
                                                  ,user.getDescription()
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
                                                  ,user.getName()
                                                  ,twitterModel.getTwitter.getScreenName()
                                                  ,user.getProfileImageURL()
                                                  ,user.getDescription()
                                                  ,twitterModel.accessToken.getToken
                                                  ,twitterModel.accessToken.getTokenSecret
                                                  ,Some(nowDate)
                                                  ,Some(nowDate))
            TwitterUser.insart(tmp)
            Option(tmp)
          }
        }
        // CookieにTwitterIdを保存し、return
        Redirect(routes.Application.index(1)).withSession("twitterId" -> String.valueOf(twitterModel.getTwitter.getId()))
      }
    }
  }

  /** 
   * ログアウト処理
   */
  def twitterLogout = Action { implicit request =>
    // Twitter ShutDown
    twitterModel.shutdown
    // Pager初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("とっぷ", 1, 0, null, Seq.empty)
    // SessionからTwitterIdを削除
    Redirect(routes.Application.index(1)).withSession(request.session - "twitterId")
  }
}
