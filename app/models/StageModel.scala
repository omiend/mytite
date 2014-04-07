package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/** Stage Master */
case class Stage (
	 id: Pk[Long]
	,stageName:String
	,createDate: Option[Date]
	,updateDate: Option[Date]
) {
}

object Stage {	
}