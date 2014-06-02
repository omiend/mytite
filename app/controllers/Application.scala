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
  // Performance登録処理用 Form
  val performanceForm = Form(
    mapping(
       "id"         -> ignored(NotAssigned: Pk[Long])
      ,"festivalId" -> of[Long]
      ,"stageId"    -> of[Long]
      ,"artist"     -> nonEmptyText
      ,"time"       -> text
      ,"timeFrame"  -> text
      ,"createDate" -> optional(date("yyyy-MM-dd"))
      ,"updateDate" -> optional(date("yyyy-MM-dd"))
    )(Performance.apply)(Performance.unapply)
  )

  // Festival/Stage登録処理用 Form
  val festivalAndStageForm = Form(
    tuple(
       "festivalName" -> nonEmptyText
      ,"stageName"    -> seq(text)
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
  def indexFestival(pageNum: Int, targetTwitterId: Long) = Action { implicit request =>
    // // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId.toLong)
      case _ => null
    }
    // Pagerを初期化
    val pager: Pager[Festival] = Pager[Festival]("フェス一覧", pageNum, 0, twitterUser, Seq.empty)
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
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : indexFestival 01")
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

    Ok(views.html.createFestival(pager, festivalAndStageForm))
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
        def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
        def nowDate: java.util.Date = new java.util.Date
        // --- Festival作成処理 --- //
        var festival: Festival = Festival(
           null
          ,twitterId.toLong
          ,festivalAndStage._1  // Festival Name
          ,Some(nowDate)
          ,Some(nowDate)
        )
        // FestivalとStageを登録する
        Festival.insartWithStage(festival, festivalAndStage._2)
        Redirect(routes.Application.indexFestival(1, twitterId.toLong))
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
      case Some(festival) => Ok(views.html.editFestival(pager, festivalId, festivalAndStageForm.fill((festival.festivalName, Seq.empty))))
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : editFestival 01")
    }
  }
  
  /*****************************************************************************
   *** Festival更新処理
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
            def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
            def nowDate: java.util.Date = new java.util.Date
            festival.festivalName = festivalAndStage._1  // Festival Name
            festival.updateDate = Some(nowDate)
            Festival.update(festival)
            Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId))
          }
        )
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : updateFestival 01")
    }
  }
  
  /*****************************************************************************
   *** Festival削除処理
   *****************************************************************************/
  def deleteFestival(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス一覧", 1, 0, twitterUser, Seq.empty)
    // 削除対象のFestivalを取得
    Festival.findById(festivalId) match {
      case Some(festival) if festival.twitterId == twitterId.toLong => {
        // 削除処理実行
        Festival.delete(festival)
        Redirect(routes.Application.indexFestival(1, twitterId.toLong))
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deleteFestival 01")
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

    Ok(views.html.createPerformance(pager, festivalId, stageSelectOptions, performanceForm))
  }

  /*****************************************************************************
   *** Performance登録処理
   *****************************************************************************/
  def insertPerformance(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("パフォーマンス新規登録画面", 1, 0, twitterUser, Seq.empty)
    // Stageを取得
    var stageSelectOptions: Seq[(String, String)] = Seq.empty
    Stage.findByFestivalId(festivalId).map { stage =>
      stageSelectOptions = stageSelectOptions :+ (stage.id.toString, stage.stageName)
    }
    performanceForm.bindFromRequest.fold(
      formWithErrors => {
        println(formWithErrors)
        BadRequest(html.createPerformance(pager, festivalId, stageSelectOptions, formWithErrors))
      },
      performance => {
        // 現在日付作成（timestamp）
        def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
        def nowDate: java.util.Date = new java.util.Date
        performance.createDate = Some(nowDate)
        performance.updateDate = Some(nowDate)
        Performance.insart(performance)
        Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId))
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
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("パフォーマンス更新画面", 1, 0, twitterUser, Seq.empty)
    Performance.findById(performanceId) match {
      case Some(performance) => {
        // Stageを取得
        var stageSelectOptions: Seq[(String, String)] = Seq.empty
        Stage.findByFestivalId(festivalId).map { stage =>
          stageSelectOptions = stageSelectOptions :+ (stage.id.toString, stage.stageName)
        }
        Ok(views.html.editPerformance(pager, performanceId, stageSelectOptions, performanceForm.fill(performance)))
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : editPerformance 01")
    }
  }

  /*****************************************************************************
   *** Performance更新処理
   *****************************************************************************/
  def updatePerformance(festivalId: Long, performanceId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("パフォーマンス更新画面", 1, 0, twitterUser, Seq.empty)
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
        def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
        def nowDate: java.util.Date = new java.util.Date
        performance.updateDate = Some(nowDate)
        Performance.update(performanceId, performance)
        Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId))
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
            Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId)).flashing("success" -> "アーティスト %s を削除しました".format(performance.artist))
          }
          case _ => Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deletePerformance 01")
        }
      }
      case _ => Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : deletePerformance 02")
    }
  }

  /*****************************************************************************
   *** TimeTable画面起動
   *****************************************************************************/
  def timeTableDetail(targetTwitterId: Long, festivalId: Long) = Action { implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId.toLong)
      case _ => null
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
            Ok(views.html.timeTableDetail(pager, targetTwitterUser, festival, stageList, createTimeTable(festivalId, stageList)))
          }
          case _ => Redirect(routes.Application.index(1)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : timeTableDetail 02")
        }
      }
      case _ => Redirect(routes.Application.index(1)).flashing("error" -> "エラーが発生しました　時間をおいてから再度お試しください - ERROR CODE : timeTableDetail 01")
    }
  }

  /** TimeTables作成 */
  def createTimeTable(festivalId: Long, stageList: Seq[Stage]): Seq[TimeTable] = {

    // Stageの名前リスト
    var stageNameList: Seq[String] = Seq.empty
    stageList.map { stage =>
      stageNameList = stageNameList :+ stage.stageName
    }

    // Performance取得
    var performanceList: Seq[Performance] = Performance.findByFestivalId(festivalId)

    // 返却用
    var timeTableList: Seq[TimeTable] = Seq.empty

    // 初期化
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
            // 
            timeTableList.collect {
              case timeTable if timeLabel == timeTable.timeLabel => {
                timeTable.performanceStageMap = timeTable.performanceStageMap + (stage.stageName -> performance)
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
        tmpTimeTable.performanceStageMap.get(stage.stageName) match {
          case Some(tmpPerformance) => {
            if (tmpPerformance.getTableRowSpanNumber > 1) {
              val deleteTimeLabel: Seq[String] = TimeTable.getTimeLabelByTargetRange(tmpPerformance.getTableRowSpanNumber, tmpPerformance.time)
              timeTableList.collect {
                case timeTable if deleteTimeLabel.contains(timeTable.timeLabel) => {
                  // Stageを削除
                  timeTable.stageList = timeTable.stageList.filter { _ != stage.stageName }
                  // 削除
                  timeTable.performanceStageMap = timeTable.performanceStageMap - stage.stageName
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
}

/**
 * Provide security features
 */
trait Secured {
  /** Retrieve the connected twitterId. */
  private def twitterId(request: RequestHeader) = request.session.get("twitterId")
  /** Redirect to login if the twitterId in not authorized. */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.TwitterController.twitterLogin)
  /** Action for authenticated users. */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(twitterId, onUnauthorized) { twitterId =>
    Action(request => f(twitterId)(request))
  }
}
