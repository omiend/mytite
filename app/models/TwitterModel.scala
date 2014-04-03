package models

import twitter4j._
import twitter4j.auth._

class TwitterModel() {

  /** コンシューマーキー */
  val consumer_key   : String = "lfOA43naUNLSqAjgpL0Q"
  /** コンシューマーシークレット */
  val consumer_secret: String = "e0A39VEr1ICgUtD7sO5nCW1diCvqQRuTRqANMj6g6E"
  /** Twitterオブジェクト */
  var requestToken: RequestToken = null

  /** Twitterオブジェクト */
  var twitter    : Twitter     = null
  var accessToken: AccessToken = null

  /** Twitterオブジェクトを取得 */
  def getTwitter: Twitter = if (twitter == null) getNewTwitter() else twitter;

  def getNewTwitter(): Twitter = {
    shutdown;
    twitter = (new TwitterFactory()).getInstance()
    twitter.setOAuthConsumer(consumer_key, consumer_secret)
    twitter
  }

  def getOAuthUrl(): String = {
    // ローカル環境
    // requestToken = getTwitter.getOAuthRequestToken("http://localhost:9000/twitterOAuthCallback")
    // Heroku
    requestToken = getTwitter.getOAuthRequestToken("http://tite-scala.herokuapp.com/twitterOAuthCallback")
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
