package models

import java.util.Date
import play.api.Play.current

/** TimeTable Structure */
case class TimeTable  (
   /** 出演時間 */
   var timeLabel: String
   /** ステージ */
  ,var stageList: Seq[String] = Seq.empty
   /** PerformanceMap（キーはステージ名） */
  ,var timeTableStageMap: Map[String, Performance] = Map.empty
) {

  /**
   * [画面用] ステージ順にPerformanceをリストで返却する
   */
  def getPerformanceList: Seq[Performance] = {
    timeTableStageMap.values.toSeq
  }

  /**
   * 指定したステージ名を削除する
   */
  def deleteStageName(deleteStageName: String) {
    stageList.filter { _ != deleteStageName }
  }
}

object TimeTable {
}
