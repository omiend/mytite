package models

/** TimeTable Structure */
case class FestivalAndStageFormModel (
   /** フェスの名前(Festival.name) */
   var festivalName: String
   /** ステージ */
  ,var stageList: Seq[String] = Seq.empty
) {
}

object FestivalAndStageFormModel {
}
