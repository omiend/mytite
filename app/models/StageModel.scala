package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/** Stage Master */
case class Stage (
	 id: Pk[Long]
  ,festivalId: Long
	,stageName:String
	,sort:String
	,color:String
	,createDate: Option[Date]
	,updateDate: Option[Date]
) {
}

object Stage {

	/**
   * Stage Simple
   */
  val simple = {
    get[Pk[Long]]("id") ~
    get[Long]("festival_id") ~
    get[String]("stage_name") ~
    get[String]("sort") ~
    get[String]("color") ~
    get[Date]("create_date") ~
    get[Date]("update_date") map {
      case id~festivalId~stageName~sort~color~createDate~updateDate => 
      Stage(
         id
        ,festivalId
        ,stageName
        ,sort
        ,color
        ,Option(createDate)
        ,Option(updateDate)
      )
    }
  }

  /**
   * Stage from-toで件数を指定して取得
   */
  def findAll: Seq[Stage] = {
    DB.withConnection { implicit connection =>
      // 親テーブル取得
      val resultList: Seq[Stage] = SQL(
        """
        select *
          from stage
         order by sort
        """
      ).as(
        Stage.simple *
      )
      resultList
    }
  }

  /**
   * Stage Insert処理
   */
  def insart(stage: Stage) {
    DB.withConnection { implicit connection =>
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
         'festival_id -> stage.festivalId
        ,'stage_name  -> stage.stageName
        ,'sort        -> stage.sort
        ,'color       -> stage.color
        ,'create_date -> stage.createDate
        ,'update_date -> stage.updateDate
      ).executeInsert()

    }
  }
}