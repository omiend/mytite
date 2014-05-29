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
    val pager: Pager[Festival] = Pager[Festival]("とっぷ", pageNum, 0, twitterUser, Seq.empty)

    // Festivalを表示するユーザーを取得する
    var targetTwitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(targetTwitterId)

    // Festival一覧を取得する
    val resultTuple = Festival.findFromTo(targetTwitterId, pager.pageNum * pager.maxListCount - pager.maxListCount, pager.maxListCount)

    // データリスト
    pager.dataList = resultTuple._1

    // 全体件数
    pager.totalRows = resultTuple._2.toInt
    
    Ok(views.html.indexFestival(pager, targetTwitterUser.head))
  }
  
  /*****************************************************************************
   *** Festival／Stage登録画面起動
   *****************************************************************************/
  def createFestival(targetTwitterId: Long) = IsAuthenticated { twitterId => implicit request =>

    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録画面", 1, 0, twitterUser, Seq.empty)

    Ok(views.html.createFestival(pager, festivalAndStageForm))
  }

  /*****************************************************************************
   *** Festival／Stage登録処理
   *****************************************************************************/
  def insertFestival = IsAuthenticated { twitterId => implicit request =>

    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録画面", 1, 0, twitterUser, Seq.empty)
    
    festivalAndStageForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.createFestival(pager, formWithErrors)).flashing("error" -> "登録に失敗しました")
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
        Redirect(routes.Application.indexFestival(1, twitterId.toLong)).flashing("success" -> "フェス %s の登録に成功しました".format(festival.festivalName))
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
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス更新画面", 1, 0, twitterUser, Seq.empty)
    // Festivalを取得
    var festival: Festival = Festival.findById(festivalId).get
    Ok(views.html.editFestival(pager, festivalId, festivalAndStageForm.fill((festival.festivalName, Seq.empty))))
  }
  
  /*****************************************************************************
   *** Festival更新処理
   *****************************************************************************/
  def updateFestival(festivalId: Long) = IsAuthenticated { twitterId => implicit request =>
    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("パフォーマンス更新画面", 1, 0, twitterUser, Seq.empty)

    // Festivalを取得
    var festival: Festival = Festival.findById(festivalId).get
    
    festivalAndStageForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.editFestival(pager, festivalId, formWithErrors)).flashing("error" -> "aaaaaaaaaaaa")
      },
      festivalAndStage => {
        def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
        def nowDate: java.util.Date = new java.util.Date
        festival.festivalName = festivalAndStage._1  // Festival Name
        festival.updateDate = Some(nowDate)
        println(festival)
        Festival.update(festival)
        Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId)).flashing("success" -> "フェス %s の更新に成功しました".format(festivalAndStage._1))
      }
    )
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
        BadRequest(html.createPerformance(pager, festivalId, stageSelectOptions, formWithErrors)).flashing("error" -> "登録に失敗しました")
      },
      performance => {
        // 現在日付作成（timestamp）
        def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
        def nowDate: java.util.Date = new java.util.Date
        performance.createDate = Some(nowDate)
        performance.updateDate = Some(nowDate)
        Performance.insart(performance)
        Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId)).flashing("success" -> "アーティスト %s の登録に成功しました".format(performance.artist))
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
    // Stageを取得
    var stageSelectOptions: Seq[(String, String)] = Seq.empty
    Stage.findByFestivalId(festivalId).map { stage =>
      stageSelectOptions = stageSelectOptions :+ (stage.id.toString, stage.stageName)
    }
    Performance.findById(performanceId).map { performance =>
      Ok(views.html.editPerformance(pager, performanceId, stageSelectOptions, performanceForm.fill(performance)))
    }.getOrElse(NotFound)
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
        BadRequest(views.html.editPerformance(pager, performanceId, stageSelectOptions, formWithErrors)).flashing("error" -> "aaaaaaaaaaaa")
      },
      performance => {
        def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
        def nowDate: java.util.Date = new java.util.Date
        performance.updateDate = Some(nowDate)
        Performance.update(performanceId, performance)
        Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId)).flashing("success" -> "アーティスト %s の更新に成功しました".format(performance.artist))
      }
    )
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
    var targetTwitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(targetTwitterId)
    // Festivalを取得する
    var festival: Option[Festival] = Festival.findById(festivalId)
    // Stageリスト取得
    val stageList: Seq[Stage] = Stage.findByFestivalId(festivalId)
    Ok(views.html.timeTableDetail(pager, targetTwitterUser.head, festival.get, stageList, createTimeTable(festivalId, stageList)))
  }

  /** TimeTables作成 */
  def createTimeTable(festivalId: Long, stageList: Seq[Stage]): Seq[TimeTable] = {

    // Stageの名前リスト
    var stageNameList: Seq[String] = Seq.empty
    stageList.map { stage =>
      stageNameList = stageNameList :+ stage.stageName
    }

    // Performance取得
    var performanceList: Seq[Performance] = Performance.findByFesticalId(festivalId)

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
