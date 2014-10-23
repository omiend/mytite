package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.features.anyToStatement

/** Performance Table */
case class Performance (
	 id            : Option[Long] = None
	,festivalId    : Long
	,var stageId   : Long
	,var artist    : String
	,var time      : String
	,var timeFrame : String
	,var createDate: Option[Date] = None
	,var updateDate: Option[Date] = None
) {
  /** TimeFrameに付随するRowSpan数を取得する */
  def getTableRowSpanNumber: Int = {
    timeFrame match {
      case TimeTable.TIME_FRAME_030 => 1
      case TimeTable.TIME_FRAME_060 => 2
      case TimeTable.TIME_FRAME_090 => 3
      case TimeTable.TIME_FRAME_120 => 4
      case _ => 0
    }
  }
}

object Performance {

	/**
   * Performance Simple
   */
  val simple = {
    get[Option[Long]]("id") ~
    get[Long]("festival_id") ~
    get[Long]("stage_id") ~
    get[String]("artist") ~
    get[String]("time") ~
    get[String]("time_frame") ~
    get[Date]("create_date") ~
    get[Date]("update_date") map {
      case id~festivalId~stageId~artist~time~timeFrame~createDate~updateDate => 
      Performance(
         id
        ,festivalId
        ,stageId
        ,artist
        ,time
        ,timeFrame
        ,Option(createDate)
        ,Option(updateDate)
      )
    }
  }

  /**
   * Performance Idを指定して取得
   */
  def findById(id: Long): Option[Performance] = {
    val params = Seq[NamedParameter](
      'id -> id
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
        select *
          from performance
         where id = {id}
         order by stage_id, time, Id
        """
      ).on(
        params: _*
      ).as(
        Performance.simple.singleOpt
      )
    }
  }

  /**
   * Performance FestivalIdを指定して取得
   */
  def findByFestivalId(festivalId: Long): Seq[Performance] = {
    val params = Seq[NamedParameter](
      'festival_id -> festivalId
    )
    DB.withConnection { implicit connection =>
      // 親テーブル取得
      val resultList: Seq[Performance] = SQL(
        """
        select *
          from performance
         where festival_id = {festival_id}
         order by stage_id, time, Id
        """
      ).on(
        params: _*
      ).as(
        Performance.simple *
      )
      resultList
    }
  }

  /**
   * Performance Insert処理
   */
  def insart(performance: Performance) {
    val params = Seq[NamedParameter](
       'festival_id -> performance.festivalId
      ,'stage_id    -> performance.stageId
      ,'artist      -> performance.artist
      ,'time        -> performance.time
      ,'time_frame  -> performance.timeFrame
      ,'create_date -> performance.createDate.get
      ,'update_date -> performance.updateDate.get
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into performance(
             festival_id
            ,stage_id
            ,artist
            ,time
            ,time_frame
            ,create_date
            ,update_date
          ) values (
             {festival_id}
            ,{stage_id}
            ,{artist}
            ,{time}
            ,{time_frame}
            ,{create_date}
            ,{update_date}
          )
        """
      ).on(
        params: _*
      ).executeInsert()
    }
  }

  /**
   * Performance Update処理
   */
  def update(performanceId: Long, performance: Performance) {
    val params = Seq[NamedParameter](
       'id          -> performanceId
      ,'stage_id    -> performance.stageId
      ,'artist      -> performance.artist
      ,'time        -> performance.time
      ,'time_frame  -> performance.timeFrame
      ,'update_date -> performance.updateDate.get
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          update performance
          set  stage_id    = {stage_id}
              ,artist      = {artist}
              ,time        = {time}
              ,time_frame  = {time_frame}
              ,update_date = {update_date}
          where id = {id}
        """
      ).on(
        params: _*
      ).executeUpdate()
    }
  }
  
  /**
   * Performance Delete処理
   */
  def delete(performance: Performance) {
    val params = Seq[NamedParameter](
      'id -> performance.id.get
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          delete from performance
          where id = {id}
        """
      ).on(
        params: _*
      ).executeUpdate()
    }
  }
}