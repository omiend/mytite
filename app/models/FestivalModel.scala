package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/** Festival Table */
case class Festival  (
   id: Pk[Long]
  ,var twitterId: Long
  ,var festivalName: String
  ,var createDate: Option[Date]
  ,var updateDate: Option[Date]
) {
}
object Festival {

  /**
   * Festival Simple
   */
  val simple = {
    get[Pk[Long]]("id") ~
    get[Long]("twitter_id") ~
    get[String]("festival_name") ~
    get[Date]("create_date") ~
    get[Date]("update_date") map {
      case id~twitterId~festivalName~createDate~updateDate => 
      Festival(
         id
        ,twitterId
        ,festivalName
        ,Option(createDate)
        ,Option(updateDate)
      )
    }
  }

  /**
   * Festival from-toで件数を指定して取得
   */
  def findFromTo(twitterId: Long, offset: Int, maxPageCount: Int) = {
    DB.withConnection { implicit connection =>

      // 親テーブル取得
      val resultList: Seq[Festival] = SQL(
        """
        select *
          from festival
         where twitter_id = {twitter_id}
         limit {maxPageCount} offset {offset}
        """
      ).on(
        'twitter_id   -> twitterId,
        'offset       -> offset,
        'maxPageCount -> maxPageCount
      ).as(
        Festival.simple *
      )

      // 件数取得
      val totalRows = SQL(
        """
        select count(*)
          from festival
         where twitter_id = {twitter_id}
        """
      ).on(
        'twitter_id -> twitterId
      ).as(scalar[Long].single)

      (resultList, totalRows)
    }
  }

  /**
   * Festival Insert処理
   */
  def insart(festival: Festival) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into festival(
             twitter_id
            ,festival_name
            ,create_date
            ,update_date
          ) values (
             {twitter_id}
            ,{festival_name}
            ,{create_date}
            ,{update_date}
          )
        """
      ).on(
         'id            -> festival.id
        ,'twitter_id    -> festival.twitterId
        ,'festival_name -> festival.festivalName
        ,'create_date   -> festival.createDate
        ,'update_date   -> festival.updateDate
      ).executeUpdate()
    }
  }

  /**
   * Festival Insert処理
   */
  def update(festival: Festival) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update festival
          set  festival_name        = {festival_name}
              ,update_date = {update_date}
          where id = {id}
        """
      ).on(
         'id            -> festival.id
        ,'festival_name -> festival.festivalName
        ,'update_date   -> festival.updateDate
      ).executeUpdate()
    }
  }
}
