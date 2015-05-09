package models

import play.api.Play.current
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

/** Festival Table */
case class Festival  (
   id              : Option[Long] = None
  ,var twitterId   : Long
  ,var festivalName: String
  ,var description : String
  ,var createDate  : Option[DateTime] = None
  ,var updateDate  : Option[DateTime] = None
) {
  var heartCount:Long = 0
}
class FestivalTable(tag: Tag) extends Table[Festival](tag, "festival") {
  def id           = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def twitterId    = column[Long]("twitter_id", O.NotNull)
  def festivalName = column[String]("festival_name", O.NotNull)
  def description  = column[String]("description", O.NotNull)
  def createDate   = column[Option[DateTime]]("create_date")
  def updateDate   = column[Option[DateTime]]("update_date")
  def * = (id, twitterId, festivalName, description, createDate, updateDate) <> ((Festival.apply _).tupled, Festival.unapply)
}
object Festival {
  lazy val query = TableQuery[FestivalTable]
  def findById(id: Long)(implicit s: Session): Option[Festival] = query.filter(_.id === id).firstOption
  def findByOffset(twitterId: Long, offset: Int, limit: Int)(implicit s: Session): Seq[Festival] = {
    (for {
      festival <- query.drop(offset).take(limit).list
    } yield {
      festival.heartCount = Heart.countByFestivalId(festival.id.get)
      festival
    })
  }
  def count()(implicit s: Session): Int = query.list.size
  def countByTwitterId(twitterId: Long)(implicit s: Session): Int = query.filter(_.twitterId === twitterId).list.size
  def insert(festival: Festival)(implicit s: Session) = query.insert(festival)
  def insertWithStage(festival: Festival, stageNameList: Seq[String])(implicit s: Session) {
    // --- Festival作成処理 --- //
    val ids = (query returning query.map(_.id)) += festival
    // --- Stage作成処理 --- //
    ids match {
      case Some(festivalInsertedId) => {
        var index: Int = 1
        val nowDate: DateTime = new DateTime
        for (stageName <- stageNameList) {
          if (!stageName.isEmpty) {
            var stage: Stage = Stage(
               None
              ,festivalInsertedId
              ,stageName
              ,Some("%03d".format(index).toString)
              ,Some("white")
              ,Some(nowDate)
              ,Some(nowDate)
            )
            Stage.insert(stage)
            index = index + 1
          }
        }
      }
      case _ => println("Festival IDが取得出来ない")
    }
  }
  def update(id: Long, festival: Festival)(implicit s: Session) = query.filter(_.id === id).update(festival)
  def delete(id: Long)(implicit s: Session) = query.filter(_.id === id).delete
}
