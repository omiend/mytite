package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

import models._

import play.api.cache._

import twitter4j._
import twitter4j.auth._

import play.api.db.slick._
import org.joda.time.DateTime

/**
 * ツイッターログインコントローラ
 */
object TwitterController extends Controller {

  /**
   * Twitterへログインする
   */
  def twitterLogin = DBAction { implicit request =>

    // Twitterオブジェクトの初期化
    val twitter: Twitter = (new TwitterFactory()).getInstance()

    // RequestTokenの取得
    val requestToken: RequestToken = twitter.getOAuthRequestToken("http://" + request.host + "/twitter/OAuthCallback")

    // TwitterとRequestTokenのオブジェクトをCacheに格納(1分有効)
    Cache.set("twitter", twitter, 60)
    Cache.set("requestToken", requestToken, 60)

    // Twitterのログイン画面にリダイレクト
    Redirect(requestToken.getAuthorizationURL())
  }

  /**
   * TwitterからのCallBack処理
   */
  def twitterOAuthCallback = DBAction { implicit request =>
    // 承認可否を精査（deniedがあったら承認キャンセル）
    request.queryString.get("denied") match {

      // Twitterのアプリケーション承認キャンセル時
      case Some(denied) => Redirect(routes.TwitterController.twitterLogout)

      // Twitterのアプリケーション承認時
      case _ => {
        // TwitterのオブジェクトをCacheから取得
        Cache.getAs[Twitter]("twitter") match {
          case Some(twitter) => {

            // RequestTokenのオブジェクトをCacheから取得
            val getRequestToken: Option[RequestToken] = Cache.getAs[RequestToken]("requestToken")

            getRequestToken match {
              case Some(requestToken) => {

                // AuthTokenを取得する
                var authToken   : String = request.queryString.get("oauth_token").get.head
                var authVerifier: String = request.queryString.get("oauth_verifier").get.head

                // AccessTokenを取得する
                val accessToken: AccessToken = twitter.getOAuthAccessToken(requestToken, authVerifier)
                twitter.getOAuthAccessToken(requestToken, authVerifier)

                // Twitterオブジェクトの認証
                twitter.verifyCredentials()

                // TwitterのUserオブジェクトを取得
                var user: User = twitter.showUser(twitter.getId())

                // TwitterUserが取得出来た場合はそのまま利用する
                def nowDate = new DateTime
                TwitterUser.findByTwitterId(twitter.getId()) match {
                  case Some(s:TwitterUser) => {
                    val id: Long = s.id.get
                    // 既にログインした事有る場合はUpdateを行う
                    val tmp: TwitterUser = new TwitterUser(Some(id)
                                                          ,twitter.getId()
                                                          ,user.getName()
                                                          ,twitter.getScreenName()
                                                          ,user.getProfileImageURL()
                                                          ,user.getDescription()
                                                          ,accessToken.getToken
                                                          ,accessToken.getTokenSecret
                                                          ,Some(nowDate)
                                                          ,Some(nowDate))
                    TwitterUser.update(id, tmp)
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
                    TwitterUser.insert(tmp)
                    Option(tmp)
                  }
                }

                // Cacheから削除
                Cache.remove("twitter")
                Cache.remove("requestToken")

                // CookieにTwitterId等を保存し、return
                Redirect(routes.Application.index(1)).withSession(
                   "twitterId"         -> String.valueOf(twitter.getId())
                  ,"accessToken"       -> accessToken.getToken
                )
              }
              case _ => Redirect(routes.Application.index(1)).flashing("error" -> "ログインに失敗しました。 - ERROR CODE : twitterOAuthCallback 02")
            }
          }
          // 取得できない場合はトップ画面へ
          case _ => Redirect(routes.Application.index(1)).flashing("error" -> "ログインに失敗しました。 - ERROR CODE : twitterOAuthCallback 01")
        }
      }
    }
  }

  /** 
   * ログアウト処理
   */
  def twitterLogout = Action { implicit request =>
    // SessionからTwitterIdを削除
    Redirect(routes.Application.index(1)).withSession(
      request.session - "twitterId" 
                      - "accessToken" 
    )
  }
}
