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
	,performanceId: Long
	,artist: String
	,time: String
	,timeFrame: String
	,createDate: Option[Date]
	,updateDate: Option[Date]
) {
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
         order by festival_id, stage_id, time, Id
        """
      ).on(
        'festival_id -> festivalId
      ).as(
        Performance.simple *
      )
      resultList
    }
  }
}