package models

import scala.slick.driver.MySQLDriver.simple._

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

/** Stage Master */
case class Stage (
	 id            : Option[Long]       = None
  ,festivalId    : Long
	,var stageName : String
	,var sort      : Option[String]     = None
	,var color     : Option[String]     = None
	,var createDate: Option[DateTime]   = None
	,var updateDate: Option[DateTime]   = None
) {
}
class StageTable(tag: Tag) extends Table[Stage](tag, "stage") {
  def id         = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def festivalId = column[Long]("festival_id")
  def stageName  = column[String]("stage_name")
  def sort       = column[Option[String]]("sort")
  def color      = column[Option[String]]("color")
  def createDate = column[Option[DateTime]]("create_date")
  def updateDate = column[Option[DateTime]]("update_date")
  def * = (id, festivalId, stageName, sort, color, createDate, updateDate) <> ((Stage.apply _).tupled, Stage.unapply)
}
object Stage {
  lazy val query = TableQuery[StageTable]
  def findById(id: Long)(implicit s: Session): Option[Stage] = query.filter(_.id === id).firstOption
  def findByFestivalId(festivalId: Long)(implicit s: Session): Seq[Stage] = query.filter(_.festivalId === festivalId).list
  def countByFestivalId(festivalId: Long)(implicit s: Session): Long = query.filter(_.festivalId === festivalId).list.size
  def insert(stage: Stage)(implicit s: Session) = query.insert(stage)
  def update(id: Long, stage: Stage)(implicit s: Session) = query.filter(_.id === id).update(stage)
  def delete(id: Long)(implicit s: Session) = query.filter(_.id === id.bind).delete
}