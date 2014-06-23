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
object Application extends Controller with Secured {

  /*****************************************************************************
   * Form
   *****************************************************************************/
  // Performance用 Form
  val performanceForm = Form(
    mapping(
       "id"         -> ignored(NotAssigned: Pk[Long])
      ,"festivalId" -> of[Long]
      ,"stageId"    -> of[Long]
      ,"artist"     -> nonEmptyText(maxLength = 20)
      ,"time"       -> text
      ,"timeFrame"  -> text
      ,"createDate" -> optional(date("yyyy-MM-dd"))
      ,"updateDate" -> optional(date("yyyy-MM-dd"))
    )(Performance.apply)(Performance.unapply)
  )

  // Stage用 Form
  val stageForm = Form(
    mapping(
       "id"         -> ignored(NotAssigned: Pk[Long])
      ,"festivalId" -> of[Long]
      ,"stageName"  -> text(maxLength = 20)
      ,"sort"       -> optional(text)
      ,"coler"      -> optional(text)
      ,"createDate" -> optional(date("yyyy-MM-dd"))
      ,"updateDate" -> optional(date("yyyy-MM-dd"))
    )(Stage.apply)(Stage.unapply)
  )

  // Festival/Stage登録処理用 Form
  val festivalAndStageForm = Form(
    tuple(
       "festivalName" -> nonEmptyText(maxLength = 20)
      ,"description"  -> text(maxLength = 50)
      ,"stageName"    -> seq(text(maxLength = 20))
    )
  )
  
  /*****************************************************************************
   * トップページ
   *****************************************************************************/
  def index(pageNum: Int) = Action { implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId.toLong)
      case _ => null
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("トップ", pageNum, 0, twitterUser, Seq.empty)
    // TwitterUserのリストを取得
    val resultTuple = TwitterUser.findFromTo(pager.pageNum * pager.maxListCount - pager.maxListCount, pager.maxListCount)
    // データリスト
    pager.dataList = resultTuple._1
    // 全体件数
    pager.totalRows = resultTuple._2.toInt
    Ok(views.html.index(pager))
  }

  /*****************************************************************************
   * Festival一覧画面起動
   *****************************************************************************/
  def festival(pageNum: Int, targetTwitterId: Long) = Action { implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId.toLong)
      case _ => null
    }
    // 参照対象のTwitterUserを取得する
    var targetTwitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(targetTwitterId)
    // Pagerを初期化
    val pager: Pager[Festival] = Pager[Festival]("@" + targetTwitterUser.get.twitterScreenName + "のフェス一覧", pageNum, 0, twitterUser, Seq.empty)
    // Festivalを表示するユーザーを取得する
    TwitterUser.getByTwitterId(targetTwitterId) match {
      case Some(targetTwitterUser) => {
        // Festival一覧を取得する
        val resultTuple = Festival.findFromTo(targetTwitterId, pager.pageNum * pager.maxListCount - pager.maxListCount, pager.maxListCount)
        // データリスト
        pager.dataList = resultTuple._1
        // 全体件数
        pager.totalRows = resultTuple._2.toInt
        Ok(views.html.indexFestival(pager, targetTwitterUser))
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : indexFestival 01")
    }
  }
  
  /*****************************************************************************
   *** Festival／Stage登録画面起動
   *****************************************************************************/
  def createFestival() = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録", 1, 0, twitterUser, Seq.empty)
    
    // Festival一覧を取得し、既に３件登録されている場合はエラー
    Festival.findFromTo(twitterId.toLong, 1, 3) match {
      case resultTuple if resultTuple._2.toInt >= 1 => Redirect(routes.Application.festival(1, twitterId.toLong)).flashing("error" -> "申し訳ございませんが、フェスは１件までしか登録できません！そのうち沢山登録出来るようにします！")
      case _ => Ok(views.html.createFestival(pager, festivalAndStageForm))
    }
  }

  /*****************************************************************************
   *** Festival／Stage登録処理
   *****************************************************************************/
  def insertFestival = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録", 1, 0, twitterUser, Seq.empty)
    festivalAndStageForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.createFestival(pager, formWithErrors))
      },
      festivalAndStage => {
        // 現在日付作成（timestamp）
        def nowDate: java.util.Date = new java.util.Date
        // --- Festival作成処理 --- //
        var festival: Festival = Festival(
           null
          ,twitterId.toLong
          ,festivalAndStage._1  // Festival Name
          ,festivalAndStage._2  // description
          ,Some(nowDate)
          ,Some(nowDate)
        )
        // FestivalとStageを登録する
        Festival.insartWithStage(festival, festivalAndStage._3)
        Redirect(routes.Application.festival(1, twitterId.toLong))
      }
    )
  }
  
  /*****************************************************************************
   *** Festival更新画面起動
   *****************************************************************************/
  def editFestival(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス更新", 1, 0, twitterUser, Seq.empty)
    // Festivalを取得
    Festival.findById(festivalId) match {
      case Some(festival) => {
        Ok(views.html.editFestival(pager, festivalId, festivalAndStageForm.fill((festival.festivalName, festival.description, Seq.empty))))
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : editFestival 01")
    }
  }
  
  /*****************************************************************************
   *** Festival処理
   *****************************************************************************/
  def updateFestival(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス更新", 1, 0, twitterUser, Seq.empty)
    // Festivalを取得
    Festival.findById(festivalId) match {
      case Some(festival) => {
        festivalAndStageForm.bindFromRequest.fold(
          formWithErrors => {
            BadRequest(views.html.editFestival(pager, festivalId, formWithErrors))
          },
          festivalAndStage => {
            def nowDate: java.util.Date = new java.util.Date
            festival.festivalName = festivalAndStage._1  // Festival Name
            festival.description = festivalAndStage._2  // description
            festival.updateDate = Some(nowDate)
            Festival.update(festival)
            Redirect(routes.Application.timetable(twitterId.toLong, festivalId))
          }
        )
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : updateFestival 01")
    }
  }
  
  /*****************************************************************************
   *** Festival削除処理
   *****************************************************************************/
  def deleteFestival(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("@" + twitterUser.get.twitterScreenName + "のフェス一覧", 1, 0, twitterUser, Seq.empty)
    // 削除対象のFestivalを取得
    Festival.findById(festivalId) match {
      case Some(festival) if festival.twitterId == twitterId.toLong => {
        // 削除処理実行
        Festival.delete(festival)
        Redirect(routes.Application.festival(1, twitterId.toLong))
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteFestival 01")
    }
  }

  /*****************************************************************************
   *** Stage登録画面起動
   *****************************************************************************/
  def createStage(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("ステージ新規登録画面", 1, 0, twitterUser, Seq.empty)
    Stage.countByFestivalId(festivalId) match {
      case count if count == 4 => Redirect(routes.Application.timetable(twitterId.toLong, festivalId)).flashing("error" -> " ステージは４件までしか登録できません")
      case _ => Ok(views.html.createStage(pager, festivalId, stageForm))
    }
  }

  /*****************************************************************************
   *** Stage登録処理
   *****************************************************************************/
  def insertStage(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("ステージ新規登録画面", 1, 0, twitterUser, Seq.empty)
    stageForm.bindFromRequest.fold(
      formWithErrors => {
        println(formWithErrors)
        BadRequest(html.createStage(pager, festivalId, formWithErrors))
      },
      stage => {
        // 現在日付作成（timestamp）
        def nowDate: java.util.Date = new java.util.Date
        stage.sort = Option("1")
        stage.color = Option("white")
        stage.createDate = Some(nowDate)
        stage.updateDate = Some(nowDate)
        Stage.insart(stage)
        Redirect(routes.Application.timetable(twitterId.toLong, festivalId))
      }
    )
  }
  
  /*****************************************************************************
   *** Stage更新画面起動
   *****************************************************************************/
  def editStage(festivalId: Long, stageId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("ステージ更新画面", 1, 0, twitterUser, Seq.empty)
    Stage.findById(stageId) match {
      case Some(stage) => {
        // Stageを取得
        var stageSelectOptions: Seq[(String, String)] = Seq.empty
        Stage.findByFestivalId(festivalId).map { stage =>
          stageSelectOptions = stageSelectOptions :+ (stage.id.toString, stage.stageName)
        }
        Ok(views.html.editStage(pager, festivalId, stageId, stageForm.fill(stage)))
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : editStage 01")
    }
  }
  
  /*****************************************************************************
   *** Stage更新処理
   *****************************************************************************/
  def updateStage(festivalId: Long, stageId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("ステージ更新画面", 1, 0, twitterUser, Seq.empty)
    stageForm.bindFromRequest.fold(
      formWithErrors => {
        println(formWithErrors)
        BadRequest(views.html.editStage(pager, festivalId, stageId, formWithErrors))
      },
      stage => {
        def nowDate: java.util.Date = new java.util.Date
        stage.updateDate = Some(nowDate)
        Stage.update(stageId, stage)
        Redirect(routes.Application.timetable(twitterId.toLong, festivalId))
      }
    )
  }
  
  /*****************************************************************************
   *** Stage削除処理
   *****************************************************************************/
  def deleteStage(festivalId: Long, stageId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス", 1, 0, twitterUser, Seq.empty)
    // 削除対象のStageを取得
    Stage.findById(stageId) match {
      case Some(stage) => {
        Festival.findById(stage.festivalId) match {
          case Some(festival) if festival.twitterId == twitterId.toLong => {
            // 削除処理実行
            Stage.delete(stage)
            Redirect(routes.Application.timetable(twitterId.toLong, festivalId))
          }
          case _ => Redirect(routes.Application.timetable(twitterId.toLong, festivalId)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteStage 01")
        }
      }
      case _ => Redirect(routes.Application.timetable(twitterId.toLong, festivalId)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteStage 02")
    }
  }
  
  /*****************************************************************************
   *** Performance登録画面起動
   *****************************************************************************/
  def createPerformance(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("パフォーマンス新規登録画面", 1, 0, twitterUser, Seq.empty)
    // Stageを取得
    var stageSelectOptions: Seq[(String, String)] = Seq.empty
    Stage.findByFestivalId(festivalId).map { stage =>
      stageSelectOptions = stageSelectOptions :+ (stage.id.toString, stage.stageName)
    }
    stageSelectOptions match {
      case stageSelectOptions if stageSelectOptions.length <= 0 => Redirect(routes.Application.timetable(twitterId.toLong, festivalId)).flashing("error" -> " 先にステージを登録してください")
      case _ => Ok(views.html.createPerformance(pager, festivalId, stageSelectOptions, performanceForm))
    }
  }

  /*****************************************************************************
   *** Performance登録処理
   *****************************************************************************/
  def insertPerformance(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("アーティスト新規登録画面", 1, 0, twitterUser, Seq.empty)
    // Stageを取得
    var stageSelectOptions: Seq[(String, String)] = Seq.empty
    Stage.findByFestivalId(festivalId).map { stage =>
      stageSelectOptions = stageSelectOptions :+ (stage.id.toString, stage.stageName)
    }
    performanceForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.createPerformance(pager, festivalId, stageSelectOptions, formWithErrors))
      },
      performance => {
        def nowDate: java.util.Date = new java.util.Date
        performance.createDate = Some(nowDate)
        performance.updateDate = Some(nowDate)
        Performance.insart(performance)
        Redirect(routes.Application.timetable(twitterId.toLong, festivalId))
      }
    )
  }

  /*****************************************************************************
   *** Performance更新画面起動
   *****************************************************************************/
  def editPerformance(festivalId: Long, performanceId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("アーティスト更新画面", 1, 0, twitterUser, Seq.empty)
    Performance.findById(performanceId) match {
      case Some(performance) => {
        // Stageを取得
        var stageSelectOptions: Seq[(String, String)] = Seq.empty
        Stage.findByFestivalId(festivalId).map { stage =>
          stageSelectOptions = stageSelectOptions :+ (stage.id.toString, stage.stageName)
        }
        Ok(views.html.editPerformance(pager, performanceId, stageSelectOptions, performanceForm.fill(performance)))
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : editPerformance 01")
    }
  }

  /*****************************************************************************
   *** Performance更新処理
   *****************************************************************************/
  def updatePerformance(festivalId: Long, performanceId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("アーティスト更新画面", 1, 0, twitterUser, Seq.empty)
    // Stageを取得
    var stageSelectOptions: Seq[(String, String)] = Seq.empty
    Stage.findByFestivalId(festivalId).map { stage =>
      stageSelectOptions = stageSelectOptions :+ (stage.id.toString, stage.stageName)
    }
    performanceForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.editPerformance(pager, performanceId, stageSelectOptions, formWithErrors))
      },
      performance => {
        def nowDate: java.util.Date = new java.util.Date
        performance.updateDate = Some(nowDate)
        Performance.update(performanceId, performance)
        Redirect(routes.Application.timetable(twitterId.toLong, festivalId))
      }
    )
  }
  
  /*****************************************************************************
   *** Performance削除処理
   *****************************************************************************/
  def deletePerformance(festivalId: Long, performanceId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス", 1, 0, twitterUser, Seq.empty)
    // 削除対象のPerformanceを取得
    Performance.findById(performanceId) match {
      case Some(performance) => {
        Festival.findById(performance.festivalId) match {
          case Some(festival) if festival.twitterId == twitterId.toLong => {
            // 削除処理実行
            Performance.delete(performance)
            Redirect(routes.Application.timetable(twitterId.toLong, festivalId))
          }
          case _ => Redirect(routes.Application.timetable(twitterId.toLong, festivalId)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deletePerformance 01")
        }
      }
      case _ => Redirect(routes.Application.timetable(twitterId.toLong, festivalId)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deletePerformance 02")
    }
  }

  /*****************************************************************************
   *** TimeTable画面起動
   *****************************************************************************/
  def timetable(targetTwitterId: Long, festivalId: Long) = Action { implicit request =>
    var isExists: (Long, Boolean) = (0, false)
    // セッションからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => {
        isExists = Heart.findByFestivalAndTwitterId(festivalId, twitterId.toLong)
        TwitterUser.getByTwitterId(twitterId.toLong)
      }
      case _ => {
        isExists = Heart.countHeartByFestivalId(festivalId)
        null
      }
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス", 1, 0, twitterUser, Seq.empty)
    // Festivalを表示するユーザーを取得する
    TwitterUser.getByTwitterId(targetTwitterId) match {
      case Some(targetTwitterUser) => {
        // Festivalを取得する
        Festival.findById(festivalId) match {
          case Some(festival) => {
            // Stageリスト取得
            val stageList: Seq[Stage] = Stage.findByFestivalId(festivalId)
            pager.title = festival.festivalName
            Ok(views.html.timeTableDetail(pager, targetTwitterUser, festival, stageList, TimeTable.createTimeTable(festivalId, stageList), isExists))
          }
          case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : timetable 02")
        }
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : timetable 01")
    }
  }

  /*****************************************************************************
   *** 静的ページ
   *****************************************************************************/
  def about() = Action { implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId.toLong)
      case _ => null
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("アバウト", 1, 0, twitterUser, Seq.empty)
    Ok(views.html.about(pager))
  }

  def usage() = Action { implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId.toLong)
      case _ => null
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("遊び方", 1, 0, twitterUser, Seq.empty)
    Ok(views.html.usage(pager))
  }
}
