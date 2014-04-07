package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/** Performance Table */
case class Performance (
	 id: Pk[Long]
	,artist: String
	,time: String
	,timeLength: String
	,createDate: Option[Date]
	,updateDate: Option[Date]
) {
}

object Performance {	
}