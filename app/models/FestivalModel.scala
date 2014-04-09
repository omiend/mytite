package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/** Festival Table */
case class Festival  (
   id        : Pk[Long]
  ,twitterId : Long
  ,name      : String
  ,createDate: Option[Date]
  ,updateDate: Option[Date]
) {
}
object Festival {

  /**
   * Festival Simple
   */
  val simple = {
    get[Pk[Long]]("id") ~
    get[Long]("twitter_id") ~
    get[String]("name") ~
    get[Date]("create_date") ~
    get[Date]("update_date") map {
      case id~twitterId~name~createDate~updateDate => 
      Fectival(
         id
        ,twitterId
        ,name
        ,Option(createDate)
        ,Option(updateDate))
    }
  }

  /**
   * Fectival witter_idを指定し、from-toで件数を指定して取得
   */
  def findFromTo(offset: Int, maxPageCount: Int) = {
    DB.withConnection { implicit connection =>

      // 取得
      val resultList: Seq[Fectival] = SQL(
        """
        select *
          from festival
         where twitter_id = {twitter_id}
         limit {maxPageCount} offset {offset}
        """
      ).on(
        'twitter_id   -> twitterId
        'offset       -> offset,
        'maxPageCount -> maxPageCount
      ).as(
        Fectival.simple *
      )

      // 件数取得
      val totalRows = SQL(
        """
        select count(*)
          from festival
        """
      ).as(scalar[Long].single)

      (resultList, totalRows)
    }
  }

  /**
   * Fectival Insert処理
   */
  def insart(festival: Fectival) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into festival(
             twitter_id
            ,name
            ,create_date
            ,update_date
          ) values (
             {twitter_id}
            ,{name}
            ,{create_date}
            ,{update_date}
          )
        """
      ).on(
         'id           -> festival.id
        ,'twitter_id   -> festival.twitterId
        ,'name         -> festival.name
        ,'create_date  -> festival.createDate
        ,'update_date  -> festival.updateDate
      ).executeUpdate()
    }
  }

  /**
   * festival Insert処理
   */
  def update(festival: Fectival) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update festival
          set  name        = {tname}
              ,update_date = {update_date}
        where id = {id}
        """
      ).on(
         'id          -> festival.id
        ,'name        -> festival.name
        ,'update_date -> festival.updateDate
      ).executeUpdate()
    }
  }
}
