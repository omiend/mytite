package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/** Festival Table */
case class Festival  (
   id: Pk[Long]
  ,twitterId: String
  ,name: String
  ,createDate: Option[Date]
  ,updateDate: Option[Date]
) {
}
object Festival {

}
