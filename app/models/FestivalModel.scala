package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.features.anyToStatement
import scala.collection.immutable.Map

/** Festival Table */
case class Festival  (
   id              : Option[Long] = None
  ,var twitterId   : Long
  ,var festivalName: String
  ,var description : String
  ,var createDate  : Option[Date] = None
  ,var updateDate  : Option[Date] = None
) {
  var heartCount:Long = 0
}
object Festival {

  /**
   * Festival Simple
   */
  val simple = {
    get[Option[Long]]("id") ~
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
   * Heart Count
   */
  val heartCount = {
    get[Option[Long]]("id") ~
    get[Long]("heartCount") map {
      case id~heartCount => (id, heartCount)
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

      // ハートの件数を取得
      SQL(
        """
        select id
               ,(select count(id) from heart where festival.id = heart.festival_id) as heartCount
          from festival
         where twitter_id = {twitter_id}
         limit {maxPageCount} offset {offset}
        """
      ).on(
        'twitter_id   -> twitterId,
        'offset       -> offset,
        'maxPageCount -> maxPageCount
      ).as(
        Festival.heartCount *
      ) map { countHeart =>
        for (festival <- resultList) {
          if (festival.id == countHeart._1) {
            festival.heartCount = countHeart._2
          }
        }
      }

      (resultList, totalRows)
    }
  }

  /**
   * Festival Insert処理
   */
  def insart(festival: Festival) {
    val params = Seq[NamedParameter](
         'twitter_id    -> festival.twitterId
        ,'festival_name -> festival.festivalName
        ,'description   -> festival.description
        ,'create_date   -> festival.createDate.get
        ,'update_date   -> festival.updateDate.get
    )
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
        params: _*
      ).executeUpdate()
    }
  }

  /**
   * Festival Insert処理
   */
  def update(festival: Festival) {
    val params = Seq[NamedParameter](
         'id            -> festival.id.get
        ,'festival_name -> festival.festivalName
        ,'description   -> festival.description
        ,'update_date   -> festival.updateDate.get
    )
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
        params: _*
      ).executeUpdate()
    }
  }

  /**
   * Festival／Stag Insert処理
   */
  def insartWithStage(festival: Festival, stageNameList: Seq[String]) {
    val params1 = Seq[NamedParameter](
         'twitter_id    -> festival.twitterId
        ,'festival_name -> festival.festivalName
        ,'description   -> festival.description
        ,'create_date   -> festival.createDate.get
        ,'update_date   -> festival.updateDate.get
    )
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
        params1: _*
      ).executeInsert()

      // --- Stage作成処理 --- //
      var index: Int = 1
      for (stageName <- stageNameList) {
        if (!stageName.isEmpty) {
          val params2 = Seq[NamedParameter](
               'festival_id -> createId.get
              ,'stage_name  -> stageName
              ,'sort        -> "%03d".format(index).toString
              ,'color       -> "white"
              ,'create_date -> festival.createDate.get
              ,'update_date -> festival.updateDate.get
          )
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
            params2: _*
          ).executeInsert()
          index = index + 1
        }
      }
    }
  }
  
  /**
   * Festival delete処理
   */
  def delete(festivalId: Long) {
    val params = Seq[NamedParameter](
      'festival_id -> festivalId
    )
    DB.withConnection { implicit connection =>
      // Performance削除処理
      SQL(
        """
          delete from Performance where festival_id = {festival_id}
        """
      ).on(
        params: _*
      ).executeUpdate()

      // Stage削除処理
      SQL(
        """
          delete from Stage where festival_id = {festival_id}
        """
      ).on(
        params: _*
      ).executeUpdate()

      // Heartを削除する
      SQL(
        """
          delete from Heart where festival_id = {festival_id}
        """
      ).on(
        params: _*
      ).executeUpdate()

      // Festival削除処理
      SQL(
        """
          delete from Festival where id = {festival_id}
        """
      ).on(
        params: _*
      ).executeUpdate()
    }
  }
}
