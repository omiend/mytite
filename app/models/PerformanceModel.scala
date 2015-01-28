package models

import play.api.Play.current
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

/** Performance Table */
case class Performance (
	 id            : Option[Long] = None
	,festivalId    : Long
	,var stageId   : Long
  ,var artist    : String
  ,var time      : String
  ,var timeFrame : String
	,var createDate: Option[DateTime] = None
	,var updateDate: Option[DateTime] = None
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
class PerformanceTable(tag: Tag) extends Table[Performance](tag, "performance") {
  def id         = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def festivalId = column[Long]("festival_id", O.NotNull)
  def stageId    = column[Long]("stage_id", O.NotNull)
  def artist     = column[String]("artist", O.NotNull)
  def time       = column[String]("time", O.NotNull)
  def timeFrame  = column[String]("time_frame", O.NotNull)
  def createDate = column[Option[DateTime]]("create_date")
  def updateDate = column[Option[DateTime]]("update_date")
  def * = (id, festivalId, stageId, artist, time, timeFrame, createDate, updateDate) <> ((Performance.apply _).tupled, Performance.unapply)
}
object Performance {
  lazy val query = TableQuery[PerformanceTable]
  def findById(id: Long)(implicit s: Session): Option[Performance] = query.filter(_.id === id).firstOption
  def findByFestivalId(festivalId: Long)(implicit s: Session): Seq[Performance] = query.filter(_.festivalId === festivalId).list
  def count()(implicit s: Session): Int = query.list.size
  def insert(performance: Performance)(implicit s: Session) = query.insert(performance)
  def update(id: Long, performance: Performance)(implicit s: Session) = query.filter(_.id === id).update(performance)
  def delete(id: Long)(implicit s: Session) = query.filter(_.id === id).delete
}
