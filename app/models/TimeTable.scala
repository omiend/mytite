package models

import java.util.Date
import play.api.Play.current
import anorm.Pk

/** TimeTable Structure */
case class TimeTable (
   val timeLabel: String
  ,var stageList: Seq[Stage]
) {

  // キーはStage.id
  var performanceStageMap: Map[Long, Performance] = Map()

  def getTimeTableList: Seq[Performance] = {
    var returnList: Seq[Performance] = Seq.empty
    for (stage <- stageList) {
      performanceStageMap.get(stage.id.get) match {
        case Some(perfor) => returnList = returnList :+ perfor
        case _            => returnList = returnList :+ Performance(null, 0, 0, "", this.timeLabel, TimeTable.TIME_FRAME_030, Some(null), Some(null))
      }
    }
    returnList
  }
}

object TimeTable {

  /** 時間 */
  val TIME_LABEL_1000: String = "10:00"
  val TIME_LABEL_1030: String = "10:30"
  val TIME_LABEL_1100: String = "11:00"
  val TIME_LABEL_1130: String = "11:30"
  val TIME_LABEL_1200: String = "12:00"
  val TIME_LABEL_1230: String = "12:30"
  val TIME_LABEL_1300: String = "13:00"
  val TIME_LABEL_1330: String = "13:30"
  val TIME_LABEL_1400: String = "14:00"
  val TIME_LABEL_1430: String = "14:30"
  val TIME_LABEL_1500: String = "15:00"
  val TIME_LABEL_1530: String = "15:30"
  val TIME_LABEL_1600: String = "16:00"
  val TIME_LABEL_1630: String = "16:30"
  val TIME_LABEL_1700: String = "17:00"
  val TIME_LABEL_1730: String = "17:30"
  val TIME_LABEL_1800: String = "18:00"
  val TIME_LABEL_1830: String = "18:30"
  val TIME_LABEL_1900: String = "19:00"
  val TIME_LABEL_1930: String = "19:30"
  val TIME_LABEL_2000: String = "20:00"
  val TIME_LABEL_2030: String = "20:30"
  val TIME_LABEL_2100: String = "21:00"
  val TIME_LABEL_2130: String = "21:30"

  /** 時間リスト */
  lazy val TIME_LABEL_LIST: Seq[String] = Seq[String](
     TIME_LABEL_1000
    ,TIME_LABEL_1030
    ,TIME_LABEL_1100
    ,TIME_LABEL_1130
    ,TIME_LABEL_1200
    ,TIME_LABEL_1230
    ,TIME_LABEL_1300
    ,TIME_LABEL_1330
    ,TIME_LABEL_1400
    ,TIME_LABEL_1430
    ,TIME_LABEL_1500
    ,TIME_LABEL_1530
    ,TIME_LABEL_1600
    ,TIME_LABEL_1630
    ,TIME_LABEL_1700
    ,TIME_LABEL_1730
    ,TIME_LABEL_1800
    ,TIME_LABEL_1830
    ,TIME_LABEL_1900
    ,TIME_LABEL_1930
    ,TIME_LABEL_2000
    ,TIME_LABEL_2030
    ,TIME_LABEL_2100
    ,TIME_LABEL_2130
  )

  // /** TIME_LABELの値を数値に変換したMap */
  // lazy val TIME_LABEL_CONVERT_INTEGER: Map[String, Int] = Map(
  //    (TIME_LABEL_1000, 1000)
  //   ,(TIME_LABEL_1030, 1030)
  //   ,(TIME_LABEL_1100, 1100)
  //   ,(TIME_LABEL_1130, 1130)
  //   ,(TIME_LABEL_1200, 1200)
  //   ,(TIME_LABEL_1230, 1230)
  //   ,(TIME_LABEL_1300, 1300)
  //   ,(TIME_LABEL_1330, 1330)
  //   ,(TIME_LABEL_1400, 1400)
  //   ,(TIME_LABEL_1430, 1430)
  //   ,(TIME_LABEL_1500, 1500)
  //   ,(TIME_LABEL_1530, 1530)
  //   ,(TIME_LABEL_1600, 1600)
  //   ,(TIME_LABEL_1630, 1630)
  //   ,(TIME_LABEL_1700, 1700)
  //   ,(TIME_LABEL_1730, 1730)
  //   ,(TIME_LABEL_1800, 1800)
  //   ,(TIME_LABEL_1830, 1830)
  //   ,(TIME_LABEL_1900, 1900)
  //   ,(TIME_LABEL_1930, 1930)
  //   ,(TIME_LABEL_2000, 2000)
  //   ,(TIME_LABEL_2030, 2030)
  //   ,(TIME_LABEL_2100, 2100)
  //   ,(TIME_LABEL_2130, 2130)
  // )
  
  /** 時間リストのSelectOptions */
  lazy val TIME_LABEL_SELECTOPTIONS: Seq[(String, String)] = {
    var returnData: Seq[(String, String)] = Seq.empty
    TIME_LABEL_LIST.map { timeLabel =>
      returnData = returnData :+ (timeLabel, timeLabel)
    }
    returnData
  }

  /** 時間枠 */
  val TIME_FRAME_030: String = "30"
  val TIME_FRAME_060: String = "60"
  val TIME_FRAME_090: String = "90"
  val TIME_FRAME_120: String = "120"
  /** 時間枠リスト */
  lazy val TIME_FRAME_LIST: Seq[String] = Seq[String](
     TIME_FRAME_030
    ,TIME_FRAME_060
    ,TIME_FRAME_090
    ,TIME_FRAME_120
  )

  /** 時間リストのSelectOptions */
  lazy val TIME_FRAME_SELECTOPTIONS: Seq[(String, String)] = {
    var returnData: Seq[(String, String)] = Seq.empty
    TIME_FRAME_LIST.map { timeLabel =>
      returnData = returnData :+ (timeLabel, timeLabel + "分")
    }
    returnData
  }

  /** 指定した数字の数だけ、指定したTIME_LABEL＋１件目の値を取得する */
  def getTimeLabelByTargetRange(targetRange: Int, timeLabel: String): Seq[String] = {
    var returnSet: Seq[String] = Seq.empty
    var targetIndex: Int = 0
    TIME_LABEL_LIST.zipWithIndex.collect {
      case timeLabelCollect if timeLabel == timeLabelCollect._1 => {
        targetIndex = timeLabelCollect._2
      }
      case _ => 
    }
    returnSet = TIME_LABEL_LIST.slice(targetIndex + 1, targetIndex + targetRange)
    returnSet
  }
  
  /** TimeTables作成 */
  def createTimeTable(festivalId: Long, stageList: Seq[Stage]): Seq[TimeTable] = {

    // Stageの名前リスト
    var stageNameList: Seq[Stage] = Seq.empty
    stageList.map { stage =>
      stageNameList = stageNameList :+ stage
    }

    // Performance取得
    var performanceList: Seq[Performance] = Performance.findByFestivalId(festivalId)

    // 返却用 & 初期化
    var timeTableList: Seq[TimeTable] = Seq.empty
    TimeTable.TIME_LABEL_LIST map { timeLabel =>
      timeTableList = timeTableList :+ TimeTable(timeLabel, stageNameList)
    }

    // 時間ラベル順に処理
    TimeTable.TIME_LABEL_LIST.map { timeLabel =>
      // ステージごとの処理
      stageList.map { stage =>
        // 取得したパフォーマンスごとの処理
        performanceList.collect {
          // パフォーマンスの時間ラベルとステージIDを指定
          case performance if timeLabel == performance.time && stage.id.get == performance.stageId => {
            timeTableList.collect {
              case timeTable if timeLabel == timeTable.timeLabel => {
                timeTable.performanceStageMap = timeTable.performanceStageMap + (stage.id.get -> performance)
              }
            }
          }
          case _ => {}
        }
      }
    }

    // --- 時間枠を表上で結合する処理 --- //
    timeTableList.map { tmpTimeTable =>
      stageList.map { stage =>
        tmpTimeTable.performanceStageMap.get(stage.id.get) match {
          case Some(tmpPerformance) => {
            if (tmpPerformance.getTableRowSpanNumber > 1) {
              val deleteTimeLabel: Seq[String] = TimeTable.getTimeLabelByTargetRange(tmpPerformance.getTableRowSpanNumber, tmpPerformance.time)
              timeTableList.collect {
                case timeTable if deleteTimeLabel.contains(timeTable.timeLabel) => {
                  // Stageを削除
                  timeTable.stageList = timeTable.stageList.filter { _ != stage }
                  // 削除
                  timeTable.performanceStageMap = timeTable.performanceStageMap - stage.id.get
                }
                case _ => {} // 何もしない
              }
            }
          }
          case _ => {} // 何もしない
        }
      }
    }

    // 返却
    timeTableList
  }
}
