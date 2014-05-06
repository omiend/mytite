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
  // Performance form
  val performanceForm = Form(
    tuple(
      "stageId"   -> text,
      "artist"    -> nonEmptyText,
      "time"      -> text,
      "timeFrame" -> text
    )
  )
  // TimeTable Form
  val timeTableForm = Form(
    mapping(
      "festivalName" -> nonEmptyText,
      "stageName"    -> seq(text)
    )(TimeTable.apply)(TimeTable.unapply)
  )
  
  /*****************************************************************************
   * トップページ
   *****************************************************************************/
  def index(pageNum: Int) = Action { implicit request =>

    // CookieからTwitterIdを取得し、取得出来た場合TwitterUserを取得する（直接Cookieから取得するのはここだけで、あとはIsAuthenticatedを使う）
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
   * Festival一覧ページ
   *****************************************************************************/
  def indexFestival(targetTwitterId: Long) = Action { implicit request =>

    // IsAuthenticatedからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId.toLong)
      case _ => null
    }

    // Pagerを初期化
    val pager: Pager[Festival] = Pager[Festival]("とっぷ", 1, 0, twitterUser, Seq.empty)

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
   *** Festival／Stage登録ページ
   *****************************************************************************/
  def createFestival(targetTwitterId: Long) = IsAuthenticated { twitterId => implicit request =>

    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録画面", 1, 0, twitterUser, Seq.empty)

    Ok(views.html.createFestival(pager, timeTableForm))
  }

  /*****************************************************************************
   *** Festival／Stage登録処理
   *****************************************************************************/
  def insertFestival = IsAuthenticated { twitterId => implicit request =>

    // IsAuthenticatedからTwitterIdを取得し、TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(twitterId.toLong)
    
    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録画面", 1, 0, twitterUser, Seq.empty)
    
    timeTableForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.createFestival(pager, formWithErrors)).flashing("error" -> "登録に失敗しました")
      },
      timeTable => {
        // 現在日付作成（timestamp）
        def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
        def nowDate: java.util.Date = new java.util.Date
        // --- Festival作成処理 --- //
        var festival: Festival = Festival(
           null
          ,twitterId.toLong
          ,timeTable.festivalName
          ,Some(nowDate)
          ,Some(nowDate)
        )
        // FestivalとStageを登録する
        Festival.insartWithStage(festival, timeTable.stageList)
        Redirect(routes.Application.indexFestival(twitterId.toLong)).flashing("success" -> "フェス %s の登録に成功しました".format(festival.festivalName))
      }
    )
  }
  
  /*****************************************************************************
   *** Performance登録ページ
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
      performanceForm => {
        // 現在日付作成（timestamp）
        def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd hh:MM:dd:ss.000").parse(str)
        def nowDate: java.util.Date = new java.util.Date
        // --- Performance作成処理 --- //
        var performance: Performance = Performance(
           null
          ,festivalId
          ,performanceForm._1.toLong // stageId
          ,performanceForm._2        // artist
          ,performanceForm._3        // time
          ,performanceForm._4        // timeLabel
          ,Some(nowDate)
          ,Some(nowDate)
        )
        // FestivalとStageを登録する
        Performance.insart(performance)
        Redirect(routes.Application.timeTableDetail(twitterId.toLong, festivalId)).flashing("success" -> "アーティスト %s の登録に成功しました".format(performanceForm._2))
      }
    )
  }

















  /**
   * タイムテーブル参照起動処理
   */
  def timeTableDetail(targetTwitterId: Long, festivalId: Long) = Action { implicit request =>

    // IsAuthenticatedからTwitterIdを取得し、取得出来た場合TwitterUserを取得する
    var twitterUser: Option[TwitterUser] = session.get("twitterId") match {
      case Some(twitterId) => TwitterUser.getByTwitterId(twitterId.toLong)
      case _ => null
    }

    // Pagerを初期化
    val pager: Pager[TwitterUser] = Pager[TwitterUser]("フェス新規登録画面", 1, 0, twitterUser, Seq.empty)
    
    // Festivalを表示するユーザーを取得する
    var targetTwitterUser: Option[TwitterUser] = TwitterUser.getByTwitterId(targetTwitterId)

    // TimeTable作成処理
    // createTimeTable(festivalId)

    Ok(views.html.timeTableDetail(pager, targetTwitterUser.head, festivalId))
  }

  /** TimeTables作成 */
  def createTimeTable(festivalId: Long): Seq[TimeTable]= {

    // Stageリスト取得
    var stageList: Seq[Stage] = Stage.findAll

    // Performance取得
    var performanceList: Seq[Performance] = Performance.findByFesticalId(festivalId)

    // 返却用
    var tmpTimeTableList: Seq[TimeTable] = Seq.empty
    var timeTableList: Seq[TimeTable] = Seq.empty

    timeTableList
  }

  // /**
  //  * Time Table List作成
  //  */
  // private void createTimeTableStructure(String twitterScreenName) {
  //  
  //     // ステージリスト取得
  //     List<Stage> stageList = StageService.getInstance().getAll();
  //     request.setAttribute("stageList", stageList);
  //  
  //     // タイムテーブル取得
  //     List<TimeTable> timeTableList = TimeTableService.getInstance().getTwitterScreenName(twitterScreenName);
  //  
  //     // 返却用
  //     List<TimeTableDto> tmpTimeTableDtoList = new ArrayList<TimeTableDto>();
  //     List<TimeTableDto> timeTableDtoList = new ArrayList<TimeTableDto>();
  //
  //     // Dto初期化
  //     for (String timeLabel : CommonConstants.getTimeLabelList()) {
  //         TimeTableDto tmpDto = new TimeTableDto(timeLabel, stageList);
  //         tmpTimeTableDtoList.add(tmpDto);
  //     }
  //
  //     // 時間ラベル順に処理
  //     for (String timeLabel : CommonConstants.getTimeLabelList()) {
  //         // ステージ順に処理
  //         for (Stage stage : stageList) {
  //             // 取得したタイムテーブルごとの処理
  //             for (TimeTable timeTable : timeTableList) {
  //                 // 時間ラベルとステージ名が合致するものを処理
  //                 if (timeLabel.equals(timeTable.getTime()) && stage.getName().equals(timeTable.getStage())) {
  //                     for (TimeTableDto dto : tmpTimeTableDtoList) {
  //                         if (timeLabel.equals(dto.getTimeLabel())) {
  //                             // 対象のDTOにTimeTableオブジェクトを設定
  //                             dto.setTimeTableStageMap(stage.getName(), timeTable);
  //                         }
  //                     }
  //                 }
  //             }
  //         }
  //     }
  //
  //     // --- 時間枠を表上で結合する処理 --- //
  //     // コピー
  //     timeTableDtoList.addAll(tmpTimeTableDtoList);
  //     for (int i = 0; i < tmpTimeTableDtoList.size(); i++) {
  //         TimeTableDto dto = tmpTimeTableDtoList.get(i);
  //         // TimeTableをステージ順に取得
  //         for (Stage stage : stageList) {
  //             TimeTable timeTable = dto.getTimeTableStageMap(stage.getName());
  //             // 当該ステージがRowspan設定されている場合、以後のTimeTableDtoのステージリストから、RowSpan設定の件数分削除
  //             if (timeTable.getTableRowSpanNumber() > 1) {
  //                 for (int j = i + 1; j < i + timeTable.getTableRowSpanNumber(); j++) {
  //                     TimeTableDto tmpDto = timeTableDtoList.get(j);
  //                     tmpDto.deleteStageName(stage.getName());
  //                     // 座標に再度格納
  //                     timeTableDtoList.set(j, tmpDto);
  //                 }
  //             }
  //         }
  //     }
  //     // リクエストスコープに格納
  //     request.setAttribute("timeTableDtoList", timeTableDtoList);
  // }

}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected twitterId.
   */
  private def twitterId(request: RequestHeader) = request.session.get("twitterId")

  /**
   * Redirect to login if the twitterId in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.TwitterController.twitterLogin)
  
  // --
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(twitterId, onUnauthorized) { twitterId =>
    Action(request => f(twitterId)(request))
  }
}
