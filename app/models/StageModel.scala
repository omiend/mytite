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