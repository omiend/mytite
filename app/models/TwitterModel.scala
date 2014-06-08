package models

import twitter4j._
import twitter4j.auth._

class TwitterModel() {

  /** Twitterオブジェクト */
  var requestToken: RequestToken = null

  /** Twitterオブジェクト */
  var twitter    : Twitter     = null
  var accessToken: AccessToken = null

  /** Twitterオブジェクトを取得 */
  def getTwitter: Twitter = if (twitter == null) getNewTwitter() else twitter

  def getNewTwitter(): Twitter = {
    shutdown;
    twitter = (new TwitterFactory()).getInstance()
    twitter
  }

  def getOAuthUrl(): String = {
    // ローカル環境
    // requestToken = getTwitter.getOAuthRequestToken("http://localhost:9000/twitterOAuthCallback")
    // Heroku tite_scala
    // requestToken = getTwitter.getOAuthRequestToken("http://tite-scala.herokuapp.com/twitterOAuthCallback")
    // Heroku mytite
    requestToken = getTwitter.getOAuthRequestToken("http://mytite.herokuapp.com/twitterOAuthCallback")
    requestToken.getAuthorizationURL()
  }

  def shutdown {
    if (twitter != null) {
      twitter.shutdown()
      twitter = null
      requestToken = null
      accessToken = null
    }
  }
}
