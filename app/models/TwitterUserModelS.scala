package models

import java.util.Date

import scala.slick.jdbc._
import scala.slick.driver.MySQLDriver.simple._

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
	,createDate              : Option[Date] = None
	,updateDate              : Option[Date] = None
) {
}

class TwitterUserTable(tag: Tag) extends Table[TwitterUser](tag, "CAT") {

  def name = column[String]("name", O.PrimaryKey)
  def color = column[String]("color", O.NotNull)

  def id                       = column[Long]  ("id",                       O.PrimaryKey)
  def twitterId                = column[Long]  ("twitterId",                O.NotNull)
  def twitterName              = column[String]("twitterName",              O.NotNull)
  def twitterScreenName        = column[String]("twitterScreenName",        O.NotNull)
  def twitterProfielImageUrl   = column[String]("twitterProfielImageUrl",   O.NotNull)
  def twitterDescription       = column[String]("twitterDescription",       O.NotNull)
  def twitterAccessToken       = column[String]("twitterAccessToken",       O.NotNull)
  def twitterAccessTokenSecret = column[String]("twitterAccessTokenSecret", O.NotNull)
  def createDate               = column[Date]  ("createDate",               O.NotNull)
  def updateDate               = column[Date]  ("updateDate",               O.NotNull)

  def * = (id
          ,twitterId
          ,twitterName
          ,twitterScreenName
          ,twitterProfielImageUrl
          ,twitterDescription
          ,twitterAccessToken
          ,twitterAccessTokenSecret
          ,createDate
          ,updateDate
  ) <> (TwitterUser.tupled, TwitterUser.unapply _)
}