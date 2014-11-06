package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.features.anyToStatement

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
  var heartCount: Long = 0
}

object TwitterUser {
  
  /**
   * TwitterUser Simple
   */
  val simple = {
    get[Option[Long]]("id") ~
    get[Long]("twitter_id") ~
    get[String]("twitter_name") ~
    get[String]("twitter_screen_name") ~
    get[String]("twitter_profiel_image_url") ~
    get[String]("twitter_description") ~
    get[String]("twitter_access_token") ~
    get[String]("twitter_access_token_secret") ~
    get[Date]("create_date") ~
    get[Date]("update_date") map {
      case id~twitterId~twitterName~twitterScreenName~twitterProfielImageUrl~twitterDescription~accessToken~accessTokenSecret~createDate~updateDate => 
      TwitterUser(
         id
        ,twitterId
        ,twitterName
        ,twitterScreenName
        ,twitterProfielImageUrl
        ,twitterDescription
        ,accessToken
        ,accessTokenSecret
        ,Option(createDate)
        ,Option(updateDate))
    }
  }

  /**
   * Heart Count
   */
  val heartCount = {
    get[Option[Long]]("id") ~
    get[Long]("heartCount") map {
      case id~heartCount => (id, heartCount)
    }
  }
  
  /**
   * Idを指定して取得
   */
  def findById(id: Long): Option[TwitterUser] = {
    val params = Seq[NamedParameter](
       'id -> id
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          select *
            from twitter_user
           where id = {id}
        """
      ).on(
        params: _*
      ).as(
        TwitterUser.simple.singleOpt
      )
    }
  }

  /**
   * ログイン認証用
   */
  def checkExistsTwitterUser(twitterID:Long, accessToken: String): Option[TwitterUser] = {
    val params = Seq[NamedParameter](
        'twitter_id                   -> twitterID
        ,'twitter_access_token        -> accessToken
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          select *
            from twitter_user
           where twitter_id = {twitter_id}
             and twitter_access_token = {twitter_access_token}
        """
      ).on(
        params: _*
      ).as(
        TwitterUser.simple.singleOpt
      )
    }
  }

  /**
   * TwitterUser twitter_idを指定して取得
   */
  def findByTwitterId(twitterId: Long): Option[TwitterUser] = {
    val params = Seq[NamedParameter](
       'twitter_id -> twitterId
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          select *
            from twitter_user
           where twitter_id = {twitter_id}
        """
      ).on(
        params: _*
      ).as(
        TwitterUser.simple.singleOpt
      )
    }
  }

  /**
   * TwitterUser twitter_screen_name を指定して取得
   */
  def findByTwitterScreenName(twitterScreenName: String): Option[TwitterUser] = {
    val params = Seq[NamedParameter](
       'twitter_screen_name -> twitterScreenName
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          select *
            from twitter_user
           where twitter_screen_name = {twitter_screen_name}
        """
      ).on(
        params: _*
      ).as(
        TwitterUser.simple.singleOpt
      )
    }
  }

  /**
   * TwitterUser from-toで件数を指定して取得
   */
  def findByOffset(offset: Int, maxPageCount: Int) = {
    val params = Seq[NamedParameter](
         'offset       -> offset
        ,'maxPageCount -> maxPageCount
    )
    DB.withConnection { implicit connection =>

      // 親テーブル取得
      val resultList: Seq[TwitterUser] = SQL(
        """
        select *
          from twitter_user
          limit {maxPageCount} offset {offset}
        """
      ).on(
        params: _*
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

      // ハートの件数を取得
      SQL(
        """
        select id
               ,(select count(h.id) 
                   from heart h, festival f 
                  where t.twitter_id = f.twitter_id
                    and f.id = h.festival_id
               ) as heartCount
          from twitter_user t
         order by heartCount desc
         limit {maxPageCount} offset {offset}
        """
      ).on(
        params: _*
      ).as(
        TwitterUser.heartCount *
      ) map { countHeart =>
        for (twitterUser <- resultList) {
          if (twitterUser.id == countHeart._1) {
            twitterUser.heartCount = countHeart._2
          }
        }
      }
      
      (resultList.sortWith(_.heartCount > _.heartCount), totalRows)
    }
  }

  /**
   * TwitterUser Insert処理
   */
  def insart(twitterUser: TwitterUser) {
    val params = Seq[NamedParameter](
         'twitter_id                  -> twitterUser.twitterId
        ,'twitter_name                -> twitterUser.twitterName
        ,'twitter_screen_name         -> twitterUser.twitterScreenName
        ,'twitter_profiel_image_url   -> twitterUser.twitterProfielImageUrl
        ,'twitter_description         -> twitterUser.twitterDescription
        ,'twitter_access_token        -> twitterUser.twitterAccessToken
        ,'twitter_access_token_secret -> twitterUser.twitterAccessTokenSecret
        ,'create_date                 -> twitterUser.createDate.get
        ,'update_date                 -> twitterUser.updateDate.get
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into twitter_user(
             twitter_id
            ,twitter_name
            ,twitter_screen_name
            ,twitter_profiel_image_url
            ,twitter_description
            ,twitter_access_token
            ,twitter_access_token_secret
            ,create_date
            ,update_date
          ) values (
             {twitter_id}
            ,{twitter_name}
            ,{twitter_screen_name}
            ,{twitter_profiel_image_url}
            ,{twitter_description}
            ,{twitter_access_token}
            ,{twitter_access_token_secret}
            ,{create_date}
            ,{update_date}
          )
        """
      ).on(
        params: _*
      ).executeUpdate()
    }
  }
  
  /**
   * TwitterUser Insert処理
   */
  def update(twitterUser: TwitterUser) {
    val params = Seq[NamedParameter](
         'id                          -> twitterUser.id.get
        ,'twitter_name                -> twitterUser.twitterName
        ,'twitter_screen_name         -> twitterUser.twitterScreenName
        ,'twitter_profiel_image_url   -> twitterUser.twitterProfielImageUrl
        ,'twitter_description         -> twitterUser.twitterDescription
        ,'twitter_access_token        -> twitterUser.twitterAccessToken
        ,'twitter_access_token_secret -> twitterUser.twitterAccessTokenSecret
        ,'update_date                 -> twitterUser.updateDate.get
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          update twitter_user
          set  twitter_name                = {twitter_name}
              ,twitter_screen_name         = {twitter_screen_name}
              ,twitter_profiel_image_url   = {twitter_profiel_image_url}
              ,twitter_description         = {twitter_description}
              ,twitter_access_token        = {twitter_access_token}
              ,twitter_access_token_secret = {twitter_access_token_secret}
              ,update_date                 = {update_date}
          where id = {id}
        """
      ).on(
        params: _*
      ).executeUpdate()
    }
  }

  def deleteAll(twitterId: Long) {
    val params = Seq[NamedParameter](
      'twitter_id -> twitterId
    )
    DB.withConnection { implicit connection =>
      // Performance削除処理
      SQL(
        """
          delete from Performance where festival_id in (select festival_id from Festival where twitter_id = {twitter_id})
        """
      ).on(
        params: _*
      ).executeUpdate()

      // Stage削除処理
      SQL(
        """
          delete from Stage where festival_id in (select festival_id from Festival where twitter_id = {twitter_id})
        """
      ).on(
        params: _*
      ).executeUpdate()

      // Heartを削除する
      SQL(
        """
          delete from Heart where twitter_id = {twitter_id}
        """
      ).on(
        params: _*
      ).executeUpdate()

      // Festival削除処理
      SQL(
        """
          delete from Festival where twitter_id = {twitter_id}
        """
      ).on(
        params: _*
      ).executeUpdate()

      // TwitterUser削除処理
      SQL(
        """
          delete from twitter_user where twitter_id = {twitter_id}
        """
      ).on(
        params: _*
      ).executeUpdate() 
    }
  }
}
