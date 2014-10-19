package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.features.anyToStatement
import play.api.data.Mapping

/** Stage Master */
case class Stage (
	 id            : Pk[Long]
  ,festivalId    : Long
	,var stageName : String
	,var sort      : Option[String]
	,var color     : Option[String]
	,var createDate: Option[Date]
	,var updateDate: Option[Date]
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
        ,Option(sort)
        ,Option(color)
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
   * Stage Idを指定して取得
   */
  def findById(id: Long): Option[Stage] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
        select *
          from stage
         where id = {id}
         order by id
        """
      ).on(
        'id -> id
      ).as(
        Stage.simple.singleOpt
      )
    }
  }
  
  /**
   * Stage from-toで件数を指定して取得
   */
  def findByFestivalId(festivalId: Long): Seq[Stage] = {
    DB.withConnection { implicit connection =>
      // 親テーブル取得
      val resultList: Seq[Stage] = SQL(
        """
        select *
          from stage
         where festival_id = {festival_id}
         order by sort
        """
      ).on(
        'festival_id -> festivalId
      ).as(
        Stage.simple *
      )
      resultList
    }
  }

  /**
   * Stageの件数を取得する
   */
  def countByFestivalId(festivalId: Long): Long = {
    DB.withConnection { implicit connection =>
      // 件数取得
      SQL(
        """
        select count(*)
          from stage
         where festival_id = {festival_id}
        """
      ).on(
        'festival_id -> festivalId
      ).as(scalar[Long].single)
    }
  }
  
  /**
   * 同じ名前のステージが登録されているかチェックする
   */
  def countByFestivalIdAndStageName(festivalId: Long, stageName: String): Boolean = {
    DB.withConnection { implicit connection =>
      // 件数取得
      val count = SQL(
        """
        select count(*)
          from stage
         where festival_id = {festival_id}
           and stage_name  = {stage_name}
        """
      ).on(
         'festival_id -> festivalId
        ,'stage_name  -> stageName
      ).as(scalar[Long].single)
      count > 0
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
        ,'create_date -> stage.createDate.get
        ,'update_date -> stage.updateDate.get
      ).executeInsert()

    }
  }

  /**
   * Stage Update処理
   */
  def update(stageId: Long, stage: Stage) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update stage
          set  stage_name  = {stage_name}
              ,update_date = {update_date}
          where id = {id}
        """
      ).on(
         'id          -> stageId
        ,'stage_name  -> stage.stageName
        ,'update_date -> stage.updateDate.get
      ).executeUpdate()
    }
  }
  
  /**
   * Stage Delete処理
   */
  def delete(stage: Stage) {
    DB.withConnection { implicit connection =>

      // Performanceを削除する
      SQL(
        """
          delete from performance
          where stage_id = {stage_id}
        """
      ).on(
        'stage_id -> stage.id.get
      ).executeUpdate()

      // Stageを削除する
      SQL(
        """
          delete from stage
          where id = {id}
        """
      ).on(
        'id -> stage.id.get
      ).executeUpdate()
    }
  }
}