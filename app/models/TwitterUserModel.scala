package models

import play.api.Play.current
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

/** Twitter Table */
case class TwitterUser (
	 id                      : Option[Long] = None
	,twitterId               : Long
  ,twitterName             : String
	,twitterScreenName       : String
	,twitterProfielImageUrl  : String
  ,twitterDescription      : String
	,twitterAccessToken      : String
	,twitterAccessTokenSecret: String
	,createDate              : Option[DateTime] = None
	,updateDate              : Option[DateTime] = None
) {
  var heartCount: Long = 111111
}
class TwitterUserTable(tag: Tag) extends Table[TwitterUser](tag, "twitter_user") {
  def id                       = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def twitterId                = column[Long]("twitter_id", O.NotNull)
  def twitterName              = column[String]("twitter_name", O.NotNull)
  def twitterScreenName        = column[String]("twitter_screen_name", O.NotNull)
  def twitterProfielImageUrl   = column[String]("twitter_profiel_image_url", O.NotNull)
  def twitterDescription       = column[String]("twitter_description", O.NotNull)
  def twitterAccessToken       = column[String]("twitter_access_token", O.NotNull)
  def twitterAccessTokenSecret = column[String]("twitter_access_token_secret", O.NotNull)
  def createDate               = column[Option[DateTime]]("create_date")
  def updateDate               = column[Option[DateTime]]("update_date")
  def * = (id, twitterId, twitterName, twitterScreenName, twitterProfielImageUrl, twitterDescription, twitterAccessToken, twitterAccessTokenSecret, createDate, updateDate) <> ((TwitterUser.apply _).tupled, TwitterUser.unapply)
}
object TwitterUser {
  lazy val query = TableQuery[TwitterUserTable]
  def findById(id: Long)(implicit s: Session): Option[TwitterUser] = query.filter(_.id === id).firstOption
  def checkExistsTwitterUser(twitterId:Long, twitterAccessToken: String)(implicit s: Session): Option[TwitterUser] = query.filter(_.twitterId === twitterId).filter(_.twitterAccessToken === twitterAccessToken).firstOption
  def findByTwitterId(twitterId: Long)(implicit s: Session): Option[TwitterUser] = query.filter(_.twitterId === twitterId).firstOption
  def findByTwitterScreenName(twitterScreenName: String)(implicit s: Session): Option[TwitterUser] = query.filter(_.twitterScreenName === twitterScreenName).firstOption
  def findByOffset(offset: Int, limit: Int)(implicit s: Session) = {
    (for {
      twitterUser <- query.drop(offset).take(limit).list
    } yield {
      twitterUser.heartCount = Heart.countByTwitterId(twitterUser.twitterId)
      twitterUser
    })
  }
  def count()(implicit s: Session): Int = query.list.size
  def insert(twitterUser: TwitterUser)(implicit s: Session) = query.insert(twitterUser)
  def update(id: Long, twitterUser: TwitterUser)(implicit s: Session) = query.filter(_.id === id).update(twitterUser)
  def deleteAll(id: Long)(implicit s: Session) = query.filter(_.id === id).delete
}
