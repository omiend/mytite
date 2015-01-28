package models

import scala.slick.driver.MySQLDriver.simple._

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

/** Heart */
case class Heart (
   id            : Option[Long] = None
  ,festivalId    : Long
  ,twitterId     : Long
  ,var createDate: Option[DateTime] = None
  ,var updateDate: Option[DateTime] = None
) {
}
class HeartTable(tag: Tag) extends Table[Heart](tag, "heart") {
  def id         = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def festivalId = column[Long]("festival_id")
  def twitterId  = column[Long]("twitter_id")
  def createDate = column[Option[DateTime]]("create_date")
  def updateDate = column[Option[DateTime]]("update_date")
  def * = (id, festivalId, twitterId, createDate, updateDate) <> ((Heart.apply _).tupled, Heart.unapply)
}
object Heart {
  lazy val query = TableQuery[HeartTable]
  def findById(id: Long)(implicit s: Session):Option[Heart] = query.filter(_.id === id).firstOption
  def findByFestivalIdAndTwitterId(festivalId: Long, twitterId: Long)(implicit s: Session): Option[Heart] = query.filter(_.festivalId === festivalId).filter(_.twitterId === twitterId).firstOption
  def countByFestivalId(festivalId: Long)(implicit s: Session): Long = query.filter(_.festivalId === festivalId).list.size
  def countByTwitterId(twitterId: Long)(implicit s: Session): Long = query.filter(_.twitterId === twitterId).list.size
  def insert(heart: Heart)(implicit s: Session) = query.insert(heart)
  def delete(festivalId:Long, twitterId: Long)(implicit s: Session) = query.filter(_.festivalId === festivalId).filter(_.twitterId === twitterId).delete
}
