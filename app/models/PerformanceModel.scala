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