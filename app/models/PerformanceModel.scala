package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/** Performance Table */
case class Performance (
	 id: Pk[Long]
	,festivalId: Long
	,stageId: Long
	,artist: String
	,time: String
	,timeFrame: String
	,createDate: Option[Date]
	,updateDate: Option[Date]
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
    get[Pk[Long]]("id") ~
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
   * Performance FestivalIdを指定して取得
   */
  def findByFesticalId(festivalId: Long): Seq[Performance] = {
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
        'festival_id -> festivalId
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
         'festival_id -> performance.festivalId
        ,'stage_id    -> performance.stageId
        ,'artist      -> performance.artist
        ,'time        -> performance.time
        ,'time_frame  -> performance.timeFrame
        ,'create_date -> performance.createDate
        ,'update_date -> performance.updateDate
      ).executeInsert()
    }
  }
}