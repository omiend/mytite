package models

import java.util.Date
import play.api.Play.current

/** TimeTable Structure */
case class TimeTable (
   /** フェスの名前(Festival.name) */
   var festivalName: String
   /** ステージ */
  ,var stageList: Seq[String] = Seq.empty
) {

  // /**
  //  * [画面用] ステージ順にPerformanceをリストで返却する
  //  */
  // def getPerformanceList: Seq[Performance] = {
  //   timeTableStageMap.values.toSeq
  // }

  // /**
  //  * 指定したステージ名を削除する
  //  */
  // def deleteStageName(deleteStageName: String) {
  //   stageList.filter { _ != deleteStageName }
  // }
}

object TimeTable {
}
