package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import anorm._
import models._
import views._

/**
 * アプリケーションコントローラ
 */
object AjaxController extends Controller with Secured {

  /*****************************************************************************
   *** Ajax用 Stage更新処理
   *****************************************************************************/
  def ajaxUpdateFestival(festivalId: Long, festivalName: String) = IsAuthenticated { twitterId => implicit request =>
    Festival.findById(festivalId) match {
      case Some(festival) if festivalName.length > 20 => BadRequest
      case Some(festival) if festival.festivalName != festivalName => {
        def nowDate: java.util.Date = new java.util.Date
        festival.festivalName = festivalName
        festival.updateDate = Some(nowDate)
        Festival.update(festival)
        Ok
      }
      case Some(festival) if festival.festivalName == festivalName => Ok
      case _ => BadRequest
    }
  }
  
  /*****************************************************************************
   *** Ajax用 Stage更新処理
   *****************************************************************************/
  def ajaxUpdateStage(stageId: Long, stageName: String) = IsAuthenticated { twitterId => implicit request =>
    Stage.findById(stageId) match {
    case Some(stage) if stageName.length > 20 => BadRequest
    case Some(stage) if stage.stageName != stageName => {
        def nowDate: java.util.Date = new java.util.Date
        stage.stageName = stageName
        stage.updateDate = Some(nowDate)
        Stage.update(stageId, stage)
        Ok
      }
      case Some(stage) if stage.stageName == stageName => Ok
      case _ => BadRequest
    }
  }
  
  /*****************************************************************************
   *** Ajax用 Performance更新処理
   *****************************************************************************/
  def ajaxUpdatePerformance(performanceId: Long, artist: String) = IsAuthenticated { twitterId => implicit request =>
    Performance.findById(performanceId) match {
      case Some(performance) if artist.length > 20 => BadRequest
      case Some(performance) if performance.artist != artist => {
        def nowDate: java.util.Date = new java.util.Date
        performance.artist = artist
        performance.updateDate = Some(nowDate)
        Performance.update(performanceId, performance)
        Ok
      }
      case Some(performance) if performance.artist == artist => Ok
      case _ => BadRequest
    }
  }

  def ajaxUpdatePerformanceByTimeFrame(performanceId: Long, stageId: Long, time: String) = IsAuthenticated { twitterId => implicit request =>
    Performance.findById(performanceId) match {
      case Some(performance)  => {
        def nowDate: java.util.Date = new java.util.Date
        performance.stageId = stageId
        performance.time = time
        performance.updateDate = Some(nowDate)
        Performance.update(performanceId, performance)
        Ok
      }
      case _ => BadRequest
    }
  }

  /*****************************************************************************
   *** Ajax用 Heart処理
   *****************************************************************************/
  def ajaxInsertHeart(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    Festival.findById(festivalId) match {
      case Some(festival) => {
        def nowDate: java.util.Date = new java.util.Date
        var heart: Heart = Heart(
           null
          ,festivalId
          ,twitterId.toLong
          ,Some(nowDate)
          ,Some(nowDate)
        )
        Heart.insert(heart)
        Ok
      }
      case _ => BadRequest
    }
  }
  def ajaxDeleteHeart(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    Heart.delete(festivalId, twitterId.toLong)
    Ok
  }
}
