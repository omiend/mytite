package models

import java.util.Date
import play.api.Play.current

/** TimeTable Structure */
case class TimeTable (
) {
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
  val TIME_LABEL_LIST: Seq[String] = Seq[String](
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
  /** 時間リストのSelectOptions */
  val TIME_LABEL_SELECTOPTIONS: Seq[(String, String)] = {
    var returnData: Seq[(String, String)] = Seq.empty
    TIME_LABEL_LIST.map { timeLabel =>
      returnData = returnData :+ (timeLabel, timeLabel)
    }
    returnData
  }
  /** 時間枠 */
  val TIME_LABEL_FRAME_030: String = "30"
  val TIME_LABEL_FRAME_060: String = "60"
  val TIME_LABEL_FRAME_090: String = "90"
  val TIME_LABEL_FRAME_120: String = "120"
  /** 時間枠リスト */
  val TIME_LABEL_FRAME_LIST: Seq[String] = Seq[String](
     TIME_LABEL_FRAME_030
    ,TIME_LABEL_FRAME_060
    ,TIME_LABEL_FRAME_090
    ,TIME_LABEL_FRAME_120
  )
  /** 時間リストのSelectOptions */
  val TIME_LABEL_FRAME_SELECTOPTIONS: Seq[(String, String)] = {
    var returnData: Seq[(String, String)] = Seq.empty
    TIME_LABEL_FRAME_LIST.map { timeLabel =>
      returnData = returnData :+ (timeLabel, timeLabel + "分")
    }
    returnData
  }
}
