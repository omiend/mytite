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
  ,var description: String
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
    get[String]("description") ~
    get[Date]("create_date") ~
    get[Date]("update_date") map {
      case id~twitterId~festivalName~description~createDate~updateDate => 
      Festival(
         id
        ,twitterId
        ,festivalName
        ,description
        ,Option(createDate)
        ,Option(updateDate)
      )
    }
  }

  /**
   * Festival Idを指定して取得
   */
  def findById(id: Long):Option[Festival] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
        select *
          from festival
         where id = {id}
        """
      ).on(
        'id -> id
      ).as(
        Festival.simple.singleOpt
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
            ,description
            ,create_date
            ,update_date
          ) values (
             {twitter_id}
            ,{festival_name}
            ,{description}
            ,{create_date}
            ,{update_date}
          )
        """
      ).on(
         'id            -> festival.id
        ,'twitter_id    -> festival.twitterId
        ,'festival_name -> festival.festivalName
        ,'description   -> festival.description
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
          set  festival_name = {festival_name}
              ,description   = {description}
              ,update_date   = {update_date}
          where id = {id}
        """
      ).on(
         'id            -> festival.id
        ,'festival_name -> festival.festivalName
        ,'description   -> festival.description
        ,'update_date   -> festival.updateDate
      ).executeUpdate()
    }
  }

  /**
   * Festival／Stag Insert処理
   */
  def insartWithStage(festival: Festival, stageNameList: Seq[String]) {
    DB.withTransaction { implicit connection =>

      // --- Festival作成処理 --- //
      val createId = SQL(
        """
          insert into festival(
             twitter_id
            ,festival_name
            ,description
            ,create_date
            ,update_date
          ) values (
             {twitter_id}
            ,{festival_name}
            ,{description}
            ,{create_date}
            ,{update_date}
          ) on duplicate key update id = LAST_INSERT_ID(id)
        """
      ).on(
         'id            -> festival.id
        ,'twitter_id    -> festival.twitterId
        ,'festival_name -> festival.festivalName
        ,'description   -> festival.description
        ,'create_date   -> festival.createDate
        ,'update_date   -> festival.updateDate
      ).executeInsert()

      // --- Stage作成処理 --- //
      var index: Int = 1
      for (stageName <- stageNameList) {
        if (!stageName.isEmpty) {
          SQL(
            """
              insert into stage(
                 festival_id
                ,stage_name
                ,sort
                ,color
                ,create_date
                ,update_date
              ) values (
                 {festival_id}
                ,{stage_name}
                ,{sort}
                ,{color}
                ,{create_date}
                ,{update_date}
              )
            """
          ).on(
             'festival_id -> createId.get
            ,'stage_name  -> stageName
            ,'sort        -> "%03d".format(index).toString
            ,'color       -> "white"
            ,'create_date -> festival.createDate
            ,'update_date -> festival.updateDate
          ).executeInsert()
          index = index + 1
        }
      }
    }
  }
  
  /**
   * Festival delete処理
   */
  def delete(festival: Festival) {
    DB.withConnection { implicit connection =>
      // Performance削除処理
      SQL(
        """
          delete from Performance where festival_id = {festival_id}
        """
      ).on(
        'festival_id -> festival.id
      ).executeUpdate()

      // Stage削除処理
      SQL(
        """
          delete from Stage where festival_id = {festival_id}
        """
      ).on(
        'festival_id -> festival.id
      ).executeUpdate()

      // Festival削除処理
      SQL(
        """
          delete from Festival where id = {id}
        """
      ).on(
        'id -> festival.id
      ).executeUpdate()
    }
  }
}
