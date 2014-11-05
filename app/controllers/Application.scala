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

  /** トップページ */
  def index(pageNum: Int) = Action { implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = request.session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.findByTwitterId(twitterId.toLong)
      case _ => None
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("トップ", pageNum, 0, twitterUser, Seq.empty)
    // TwitterUserのリストを取得
    val resultTuple = TwitterUser.findByOffset(pager.pageNum * pager.maxListCount - pager.maxListCount, pager.maxListCount)
    // データリスト
    pager.dataList = resultTuple._1
    // 全体件数
    pager.totalRows = resultTuple._2.toInt
    Ok(views.html.index(pager))
  }

  /*****************************************************************************
   * Festival一覧画面起動
   *****************************************************************************/
  def festival(pageNum: Int, twitterScreenName: String) = Action { implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = request.session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.findByTwitterId(twitterId.toLong)
      case _ => None
    }
    // 参照対象のTwitterUserを取得する
    TwitterUser.findByTwitterScreenName(twitterScreenName) match {
      case Some(targetTwitterUser) => {
        // Pagerを初期化
        val pager: Pager[Festival] = Pager[Festival]("@" + targetTwitterUser.twitterScreenName + "のフェス一覧", pageNum, 0, twitterUser, Seq.empty)
        // Festival一覧を取得する
        val resultTuple = Festival.findByOffset(targetTwitterUser.twitterId, pager.pageNum * pager.maxListCount - pager.maxListCount, pager.maxListCount)
        // データリスト
        pager.dataList = resultTuple._1
        // 全体件数
        pager.totalRows = resultTuple._2.toInt
        Ok(views.html.indexFestival(pager, targetTwitterUser))
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : festival 01")
    }
  }

  /*****************************************************************************
   *** Festival／Stage登録画面起動
   *****************************************************************************/
  def createFestival = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    TwitterUser.findByTwitterId(twitterId.toLong) match {
      case Some(twitterUser) => {
        // Pagerを初期化
        val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録", 1, 0, Some(twitterUser), Seq.empty)
        // Festival一覧を取得し、既に登録されている場合はエラー
        Festival.findByOffset(twitterUser.twitterId, 1, 2) match {
          case resultTuple if resultTuple._2.toInt >= 1 => Redirect(routes.Application.festival(1, twitterUser.twitterScreenName)).flashing("error" -> "申し訳ございませんが、フェスは１件までしか登録できません！そのうち沢山登録出来るようにします！")
          case _ => Ok(views.html.createFestival(pager, festivalAndStageForm))
        }
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : createFestival 01")
    }
  }
  
  /*****************************************************************************
   *** Festival／Stage登録処理
   *****************************************************************************/
  def insertFestival = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    TwitterUser.findByTwitterId(twitterId.toLong) match {
      case Some(twitterUser) => {
        // Pagerを初期化
        val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録", 1, 0, Some(twitterUser), Seq.empty)
        festivalAndStageForm.bindFromRequest.fold(
          formWithErrors => {
            BadRequest(html.createFestival(pager, formWithErrors))
          },
          festivalAndStage => {
            // 現在日付作成（timestamp）
            def nowDate: java.util.Date = new java.util.Date
            // --- Festival作成処理 --- //
            var festival: Festival = Festival(
               None
              ,twitterUser.twitterId
              ,festivalAndStage._1  // Festival Name
              ,festivalAndStage._2  // description
              ,Some(nowDate)
              ,Some(nowDate)
            )
            // FestivalとStageを登録する
            Festival.insartWithStage(festival, festivalAndStage._3)
            Redirect(routes.Application.festival(1, twitterUser.twitterScreenName))
          }
        )
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : insertFestival 01")
    }
  }
  
  /*****************************************************************************
   *** Festival更新画面起動
   *****************************************************************************/
  def editFestival(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
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
    TwitterUser.findByTwitterId(twitterId.toLong) match {
      case Some(twitterUser) => {
        // Pagerを初期化
        val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス更新", 1, 0, Some(twitterUser), Seq.empty)
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
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : updateFestival 02")
    }
  }
  
  /*****************************************************************************
   *** Festival削除処理
   *****************************************************************************/
  def deleteFestival(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    TwitterUser.findByTwitterId(twitterId.toLong) match {
      case Some(twitterUser) => {
        // Pagerを初期化
        val pager: Pager[TwitterUser] = Pager[TwitterUser]("@" + twitterUser.twitterScreenName + "のフェス一覧", 1, 0, Some(twitterUser), Seq.empty)
        // 削除対象のFestivalを取得
        Festival.findById(festivalId) match {
          // ログインしているTwitterIDと同じIDの場合だけ削除を実行する
          case Some(festival) if festival.twitterId == twitterUser.twitterId => {
            // 削除処理実行
            Festival.delete(festivalId)
            Redirect(routes.Application.festival(1, twitterUser.twitterScreenName))
          }
          case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteFestival 01")
        }
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteFestival 02")
    }
  }

  /*****************************************************************************
   *** Stage登録画面起動
   *****************************************************************************/
  def createStage(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    TwitterUser.findByTwitterId(twitterId.toLong) match {
      case Some(twitterUser) => {
        // Pagerを初期化
        val pager: Pager[TwitterUser] = Pager[TwitterUser]("ステージ新規登録画面", 1, 0, Some(twitterUser), Seq.empty)
        Stage.countByFestivalId(festivalId) match {
          case count if count == 4 => Redirect(routes.Application.timetable(twitterUser.twitterId, festivalId)).flashing("error" -> " ステージは４件までしか登録できません")
          case _ => Ok(views.html.createStage(pager, festivalId, stageForm))
        }
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : createStage 01")
    }
  }

  /*****************************************************************************
   *** Stage登録処理
   *****************************************************************************/
  def insertStage(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    TwitterUser.findByTwitterId(twitterId.toLong) match {
      case Some(twitterUser) => {
        // Pagerを初期化
        val pager: Pager[TwitterUser] = Pager[TwitterUser]("ステージ新規登録画面", 1, 0, Some(twitterUser), Seq.empty)
        stageForm.bindFromRequest.fold(
          formWithErrors => {
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
            Redirect(routes.Application.timetable(twitterUser.twitterId, festivalId))
          }
        )
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : insertStage 02")
    }
  }
  
  /*****************************************************************************
   *** Stage更新画面起動
   *****************************************************************************/
  def editStage(festivalId: Long, stageId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("ステージ更新画面", 1, 0, twitterUser, Seq.empty)
    Stage.findById(stageId) match {
      case Some(stage) => Ok(views.html.editStage(pager, festivalId, stageId, stageForm.fill(stage)))
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : editStage 01")
    }
  }
  
  /*****************************************************************************
   *** Stage更新処理
   *****************************************************************************/
  def updateStage(festivalId: Long, stageId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("ステージ更新画面", 1, 0, twitterUser, Seq.empty)
    stageForm.bindFromRequest.fold(
      formWithErrors => {
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
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス", 1, 0, twitterUser, Seq.empty)
    // 削除対象のStageを取得
    Stage.findById(stageId) match {
      case Some(stage) => {
        Festival.findById(stage.festivalId) match {
          case Some(festival) if festival.twitterId == twitterId.toLong => {
            // 削除処理実行
            Stage.delete(stageId)
            Redirect(routes.Application.timetable(twitterId.toLong, festivalId))
          }
          case _ => Redirect(routes.Application.timetable(twitterId.toLong, festivalId)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteStage 01")
        }
      }
      case _ => Redirect(routes.Application.timetable(twitterId.toLong, festivalId)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteStage 02")
    }
  }
  
  /** StageのSelectOptionを作成する */
  def getStageOptions(festivalId: Long): Seq[(String, String)] = {
    var stageSelectOptions: Seq[(String, String)] = Seq.empty
    if (stageSelectOptions.length <= 0) {
      Stage.findByFestivalId(festivalId).map { stage =>
        stage.id match {
          case Some(stageId) => stageSelectOptions = stageSelectOptions :+ (stageId.toString, stage.stageName)
          case _ =>
        }
      }
    }
    stageSelectOptions
  }

  /*****************************************************************************
   *** Performance登録画面起動
   *****************************************************************************/
  def createPerformance(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("パフォーマンス新規登録画面", 1, 0, twitterUser, Seq.empty)
    val stageSelectOptions = getStageOptions(festivalId)
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
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("アーティスト新規登録画面", 1, 0, twitterUser, Seq.empty)
    performanceForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.createPerformance(pager, festivalId, getStageOptions(festivalId), formWithErrors))
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
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("アーティスト更新画面", 1, 0, twitterUser, Seq.empty)
    Performance.findById(performanceId) match {
      case Some(performance) => Ok(views.html.editPerformance(pager, festivalId, performanceId, getStageOptions(festivalId), performanceForm.fill(performance)))
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : editPerformance 01")
    }
  }

  /*****************************************************************************
   *** Performance更新処理
   *****************************************************************************/
  def updatePerformance(festivalId: Long, performanceId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("アーティスト更新画面", 1, 0, twitterUser, Seq.empty)
    performanceForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.editPerformance(pager, festivalId, performanceId, getStageOptions(festivalId), formWithErrors))
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
    var twitterUser: Option[TwitterUser] = TwitterUser.findByTwitterId(twitterId.toLong)
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
    var twitterUser: Option[TwitterUser] = request.session.get("twitterId") match {
      case Some(twitterId) => {
        isExists = Heart.findByFestivalAndTwitterId(festivalId, twitterId.toLong)
        TwitterUser.findByTwitterId(twitterId.toLong)
      }
      case _ => {
        isExists = Heart.countHeartByFestivalId(festivalId)
        None
      }
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス", 1, 0, twitterUser, Seq.empty)
    // Festivalを表示するユーザーを取得する
    TwitterUser.findByTwitterId(targetTwitterId) match {
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
   *** 退会ページ
   *****************************************************************************/
  def withdraw = IsAuthenticated { twitterId => implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = request.session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.findByTwitterId(twitterId.toLong)
      case _ => None
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("退会ページ", 1, 0, twitterUser, Seq.empty)
    Ok(views.html.withdraw(pager))
  }

  def deleteAll = IsAuthenticated { twitterId => implicit request =>
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("トップ", 1, 0, None, Seq.empty)
    TwitterUser.findByTwitterId(twitterId.toLong) match {
      case Some(twitterUser) => {
        TwitterUser.deleteAll(twitterId.toLong)
        Redirect(routes.TwitterController.twitterLogout)
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> " エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteAll 01")
    }
  }

  /*****************************************************************************
   *** 静的ページ
   *****************************************************************************/
  def about = Action { implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = request.session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.findByTwitterId(twitterId.toLong)
      case _ => None
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("アバウト", 1, 0, twitterUser, Seq.empty)
    Ok(views.html.about(pager))
  }

  def usage = Action { implicit request =>
    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = request.session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.findByTwitterId(twitterId.toLong)
      case _ => None
    }
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("遊び方", 1, 0, twitterUser, Seq.empty)
    Ok(views.html.usage(pager))
  }

  /*****************************************************************************
   * Form
   *****************************************************************************/
  // Performance用 Form
  val performanceForm = Form(
    mapping(
       "id"         -> ignored(None: Option[Long])
      ,"festivalId" -> of[Long]
      ,"stageId"    -> of[Long]
      ,"artist"     -> nonEmptyText(maxLength = 35)
      ,"time"       -> text
      ,"timeFrame"  -> text
      ,"createDate" -> optional(date("yyyy-MM-dd"))
      ,"updateDate" -> optional(date("yyyy-MM-dd"))
    )(Performance.apply)(Performance.unapply)
  )

  // Stage用 Form
  val stageForm = Form(
    mapping(
       "id"         -> ignored(None: Option[Long])
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
}
