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
	 id            : Option[Long]   = None
  ,festivalId    : Long
	,var stageName : String
	,var sort      : Option[String] = None
	,var color     : Option[String] = None
	,var createDate: Option[Date]   = None
	,var updateDate: Option[Date]   = None
) {
}

object Stage {

	/**
   * Stage Simple
   */
  val simple = {
    get[Option[Long]]("id") ~
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
    val params = Seq[NamedParameter](
      'id -> id
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
        select *
          from stage
         where id = {id}
         order by id
        """
      ).on(
        params: _*
      ).as(
        Stage.simple.singleOpt
      )
    }
  }
  
  /**
   * Stage from-toで件数を指定して取得
   */
  def findByFestivalId(festivalId: Long): Seq[Stage] = {
    val params = Seq[NamedParameter](
      'festival_id -> festivalId
    )
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
        params: _*
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
    val params = Seq[NamedParameter](
      'festival_id -> festivalId
    )
    DB.withConnection { implicit connection =>
      // 件数取得
      SQL(
        """
        select count(*)
          from stage
         where festival_id = {festival_id}
        """
      ).on(
        params: _*
      ).as(scalar[Long].single)
    }
  }
  
  /**
   * 同じ名前のステージが登録されているかチェックする
   */
  def countByFestivalIdAndStageName(festivalId: Long, stageName: String): Boolean = {
    val params = Seq[NamedParameter](
       'festival_id -> festivalId
      ,'stage_name  -> stageName
    )
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
        params: _*
      ).as(scalar[Long].single)
      count > 0
    }
  }
  
  /**
   * Stage Insert処理
   */
  def insart(stage: Stage) {
    val params = Seq[NamedParameter](
       'festival_id -> stage.festivalId
      ,'stage_name  -> stage.stageName
      ,'sort        -> stage.sort.get
      ,'color       -> stage.color.get
      ,'create_date -> stage.createDate.get
      ,'update_date -> stage.updateDate.get
    )
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
        params: _*
      ).executeInsert()

    }
  }
  
  /**
   * Stage Update処理
   */
  def update(stageId: Long, stage: Stage) {
    val params = Seq[NamedParameter](
       'id          -> stageId
      ,'stage_name  -> stage.stageName
      ,'update_date -> stage.updateDate.get
    )
    DB.withConnection { implicit connection =>
      SQL(
        """
          update stage
          set  stage_name  = {stage_name}
              ,update_date = {update_date}
          where id = {id}
        """
      ).on(
        params: _*
      ).executeUpdate()
    }
  }
  
  /**
   * Stage Delete処理
   */
  def delete(stageId: Long) {
    val params = Seq[NamedParameter](
       'stage_id -> stageId
    )
    DB.withConnection { implicit connection =>
      // Performanceを削除する
      SQL(
        """
          delete from performance
          where stage_id = {stage_id}
        """
      ).on(
        params: _*
      ).executeUpdate()

      // Stageを削除する
      SQL(
        """
          delete from stage
          where id = {stage_id}
        """
      ).on(
        params: _*
      ).executeUpdate()
    }
  }
}