package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.data.Mapping

/** Heart */
case class Heart (
   id            : Option[Long] = None
  ,festivalId    : Long
  ,twitterId     : Long
  ,var createDate: Option[Date] = None
  ,var updateDate: Option[Date] = None
) {
}

object Heart {

  /**
   * Heart Simple
   */
  val simple = {
    get[Option[Long]]("id") ~
    get[Long]("festival_id") ~
    get[Long]("twitter_id") ~
    get[Date]("create_date") ~
    get[Date]("update_date") map {
      case id~festivalId~twitterId~createDate~updateDate => 
      Heart(
         id
        ,festivalId
        ,twitterId
        ,Option(createDate)
        ,Option(updateDate)
      )
    }
  }

  /**
   * Heartの件数を取得する
   */
  def countByFestivalId(festivalId: Long): Long = {
    val params = Seq[NamedParameter](
      'festival_id -> festivalId
    )
    DB.withConnection { implicit connection =>
      // 件数取得
      SQL(
        """
        select count(*)
          from Heart
         where festival_id = {festival_id}
        """
      ).on(
        params: _*
      ).as(scalar[Long].single)
    }
  }

  /**
   * Heartの件数を取得する
   */
  def countHeartByFestivalId(festivalId: Long): (Long, Boolean) = {
    val params = Seq[NamedParameter](
      'festival_id -> festivalId
    )
    DB.withConnection { implicit connection =>
      // 件数取得
      val count: Long = SQL(
        """
        select count(*)
          from Heart
         where festival_id = {festival_id}
        """
      ).on(
        params: _*
      ).as(scalar[Long].single)
      (count, false)
    }
  }
  
  /**
   * Heartの件数を取得する
   */
  def findByFestivalAndTwitterId(festivalId: Long, twitterId: Long): (Long, Boolean) = {
    val params = Seq[NamedParameter](
         'festival_id -> festivalId
        ,'twitter_id  -> twitterId
    )
    DB.withConnection { implicit connection =>
      // 件数取得
      val count: Long = SQL(
        """
        select count(*)
          from Heart
         where festival_id = {festival_id}
        """
      ).on(
        params: _*
      ).as(scalar[Long].single)

      // 登録済みか取得する
      val isExists: Boolean = SQL(
        """
        select *
          from Heart
         where festival_id = {festival_id}
           and twitter_id = {twitter_id}
         limit 1
        """
      ).on(
        params: _*
      ).as(
        Heart.simple.singleOpt
      ) match {
        case Some(heart) => true
        case _           => false
      }
      (count, isExists)
    }
  }

  /**
   * Heart Insert処理
   */
  def insert(heart: Heart) {
    val params = Seq[NamedParameter](
         'festival_id -> heart.festivalId
        ,'twitter_id  -> heart.twitterId
        ,'create_date -> heart.createDate
        ,'update_date -> heart.updateDate
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into Heart(
             festival_id
            ,twitter_id
            ,create_date
            ,update_date
          ) values (
             {festival_id}
            ,{twitter_id}
            ,{create_date}
            ,{update_date}
          )
        """
      ).on(
         'festival_id -> heart.festivalId
        ,'twitter_id  -> heart.twitterId
        ,'create_date -> heart.createDate
        ,'update_date -> heart.updateDate
      ).executeInsert()

    }
  }

  /**
   * Heart Delete処理
   */
  def delete(festivalId:Long, twitterId: Long) {
    DB.withConnection { implicit connection =>
      // Heartを削除する
      SQL(
        """
          delete from Heart
          where festival_id = {festival_id}
            and twitter_id = {twitter_id}
        """
      ).on(
         'festival_id -> festivalId
        ,'twitter_id  -> twitterId
      ).executeUpdate()
    }
  }
}