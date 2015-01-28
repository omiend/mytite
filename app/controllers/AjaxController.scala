package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

import models._
import views._

import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._

import org.joda.time.DateTime

/**
 * アプリケーションコントローラ
 */
object AjaxController extends Controller with Secured {

  /*****************************************************************************
   *** Ajax用 Festival更新処理
   *****************************************************************************/
  def ajaxUpdateFestival(festivalId: Long, festivalName: String) = IsAuthenticated { twitterId => implicit request =>
    DB.withSession { implicit session => 
      Festival.findById(festivalId) match {
        case Some(festival) if festivalName.length > 20 => BadRequest
        case Some(festival) if festival.festivalName != festivalName => {
          def nowDate = new DateTime
          festival.festivalName = festivalName
          festival.updateDate = Some(nowDate)
          Festival.update(festivalId, festival)
          Ok
        }
        case Some(festival) if festival.festivalName == festivalName => Ok
        case _ => BadRequest
      }
    }
  }
  
  /*****************************************************************************
   *** Ajax用 Stage更新処理
   *****************************************************************************/
  def ajaxUpdateStage(stageId: Long, stageName: String) = IsAuthenticated { twitterId => implicit request =>
    DB.withSession{ implicit session => 
      Stage.findById(stageId) match {
      case Some(stage) if stageName.length > 20 => BadRequest
      case Some(stage) if stage.stageName != stageName => {
          def nowDate = new DateTime
          stage.stageName = stageName
          stage.updateDate = Some(nowDate)
          Stage.update(stageId, stage)
          Ok
        }
        case Some(stage) if stage.stageName == stageName => Ok
        case _ => BadRequest
      }
    }
  }
  
  /*****************************************************************************
   *** Ajax用 Performance更新処理
   *****************************************************************************/
  def ajaxUpdatePerformance(performanceId: Long, artist: String) = IsAuthenticated { twitterId => implicit request =>
    DB.withSession{ implicit session => 
      Performance.findById(performanceId) match {
        case Some(performance) if artist.length > 35 => BadRequest
        case Some(performance) if performance.artist != artist => {
          val nowDate: DateTime = new DateTime
          performance.artist = artist
          performance.updateDate = Some(nowDate)
          Performance.update(performanceId, performance)
          Ok
        }
        case Some(performance) if performance.artist == artist => Ok
        case _ => BadRequest
      }
    }
  }

  def ajaxUpdatePerformanceByTimeFrame(performanceId: Long, stageId: Long, time: String) = IsAuthenticated { twitterId => implicit request =>
    DB.withSession{ implicit session => 
      Performance.findById(performanceId) match {
        case Some(performance)  => {
          val nowDate: DateTime = new DateTime
          performance.stageId = stageId
          performance.time = time
          performance.updateDate = Some(nowDate)
          Performance.update(performanceId, performance)
          Ok
        }
        case _ => BadRequest
      }
    }
  }

  /*****************************************************************************
   *** Ajax用 Heart処理
   *****************************************************************************/
  def ajaxInsertHeart(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    DB.withSession{ implicit session => 
      Festival.findById(festivalId) match {
        case Some(festival) => {
          def nowDate = new DateTime
          var heart: Heart = Heart(
             None
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
  }
  def ajaxDeleteHeart(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    DB.withSession{ implicit session => 
      Heart.delete(festivalId, twitterId.toLong)
      Ok
    }
  }
}
