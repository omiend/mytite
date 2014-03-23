package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/** Twitter Table */
case class TwitterUser (
	id: Pk[Long]
	,twitterId: Long
	,twitterScreenName: String
	,twitterProfielImageUrl: String
	,twitterAccessToken: String
	,twitterAccessTokenSecret: String
	,createDate: Option[Date]
	,updateDate: Option[Date]
) {
}

object TwitterUser {
  
  /**
   * TwitterUser Simple
   */
  val simple = {
    get[Pk[Long]]("id") ~
    get[Long]("twitter_id") ~
    get[String]("twitter_screen_name") ~
    get[String]("twitter_profiel_image_url") ~
    get[String]("twitter_access_token") ~
    get[String]("twitter_access_token_secret") ~
    get[Date]("create_date") ~
    get[Date]("update_date") map {
      case id~twitterId~twitterScreenName~twitterProfielImageUrl~accessToken~accessTokenSecret~createDate~updateDate => 
      TwitterUser(
         id
        ,twitterId
        ,twitterScreenName
        ,twitterProfielImageUrl
        ,accessToken
        ,accessTokenSecret
        ,Option(createDate)
        ,Option(updateDate))
    }
  }

  /**
   * TwitterUser twitter_idを指定して取得
   */
  def getByTwitterId(twitterId: String): Option[TwitterUser] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select *
            from twitter_user
           where twitter_id = {twitter_id}
        """
      ).on(
        'twitter_id -> twitterId
      ).as(
        TwitterUser.simple.singleOpt
      )
    }
  }

  /**
   * TwitterUser from-toで件数を指定して取得
   */
  def findFromTo(offset: Int, maxPageCount: Int) = {
    DB.withConnection { implicit connection =>

      // 親テーブル取得
      val resultList: Seq[TwitterUser] = SQL(
        """
        select *
          from twitter_user
          limit {maxPageCount} offset {offset}
        """
      ).on(
        'offset -> offset,
        'maxPageCount -> maxPageCount
      ).as(
        TwitterUser.simple *
      )

      // 件数取得
      val totalRows = SQL(
        """
        select count(*)
          from twitter_user
        """
      ).as(scalar[Long].single)

      (resultList, totalRows)
    }
  }

  /**
   * TwitterUser Insert処理
   */
  def insart(twitterUser: TwitterUser) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into twitter_user(
             twitter_id
            ,twitter_screen_name
            ,twitter_profiel_image_url
            ,twitter_access_token
            ,twitter_access_token_secret
            ,create_date
            ,update_date
          ) values (
             {twitter_id}
            ,{twitter_screen_name}
            ,{twitter_profiel_image_url}
            ,{twitter_access_token}
            ,{twitter_access_token_secret}
            ,{create_date}
            ,{update_date}
          )
        """
      ).on(
         'id                          -> twitterUser.id
        ,'twitter_id                  -> twitterUser.twitterId
        ,'twitter_screen_name         -> twitterUser.twitterScreenName
        ,'twitter_profiel_image_url   -> twitterUser.twitterProfielImageUrl
        ,'twitter_access_token        -> twitterUser.twitterAccessToken
        ,'twitter_access_token_secret -> twitterUser.twitterAccessTokenSecret
        ,'create_date                 -> twitterUser.createDate
        ,'update_date                 -> twitterUser.updateDate
      ).executeUpdate()
    }
  }

  /**
   * TwitterUser Insert処理
   */
  def update(twitterUser: TwitterUser) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update twitter_user
          set  twitter_screen_name         = {twitter_screen_name}
              ,twitter_profiel_image_url   = {twitter_profiel_image_url}
              ,twitter_access_token        = {twitter_access_token}
              ,twitter_access_token_secret = {twitter_access_token_secret}
              ,update_date                 = {update_date}
          where id = {id}
        """
      ).on(
         'id                          -> twitterUser.id
        ,'twitter_screen_name         -> twitterUser.twitterScreenName
        ,'twitter_profiel_image_url   -> twitterUser.twitterProfielImageUrl
        ,'twitter_access_token        -> twitterUser.twitterAccessToken
        ,'twitter_access_token_secret -> twitterUser.twitterAccessTokenSecret
        ,'update_date                 -> twitterUser.updateDate
      ).executeUpdate()
    }
  }
}

/** Festival Table */
case class Festival  (
	id: Pk[Long]
	,twitterId: String
	,name: String
	,createDate: Option[Date]
	,updateDate: Option[Date]
) {
}
object Festival {	
}

/** Performance Table */
case class Performance (
	id: Pk[Long]
	,artist: String
	,time: String
	,timeLength: String
	,createDate: Option[Date]
	,updateDate: Option[Date]
) {
}
object Performance {	
}

/** Stage Master */
case class Stage (
	id: Pk[Long]
	,stageName:String
	,createDate: Option[Date]
	,updateDate: Option[Date]
) {
}
object Stage {	
}
