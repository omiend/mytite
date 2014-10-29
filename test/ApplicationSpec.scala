import java.io.File
import java.util.Date
import java.text.SimpleDateFormat;

import models._

import anorm._
import anorm.SqlParser._
import anorm.SimpleSql

import play.api.db._
import play.api.db.DBPlugin

import play.api.Play.current

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.{GlobalSettings, Application}
import play.api.test._
import play.api.test.Helpers._
import play.api.test.FakeApplication

import org.specs2.specification._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  def fakeApp = FakeApplication(additionalConfiguration = inMemoryDatabase())

  "Application" should {
    
    // GET /hoge (send 404 on a bad request)
    "GET  /hoge (send 404 on a bad request)" in new WithApplication(fakeApp) {
      route(FakeRequest(GET, "/hoge")) must beNone
    }

    // GET     /                           controllers.Application.index(p: Int ?= 1)
    "GET  / " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableHeart)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // GET     /usage                      controllers.Application.usage
    "GET  /usage " in new WithApplication(fakeApp) {
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/usage")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // GET     /about                      controllers.Application.about
    "GET  /about " in new WithApplication(fakeApp) {
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/about")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // GET     /festival/:targetTwitterId                    controllers.Application.festival(p: Int ?= 1, targetTwitterId: Long)
    "GET  /festival/:targetTwitterId " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/festival/900001")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // GET     /createFestival                               controllers.Application.createFestival
    "GET  /createFestival " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/createFestival").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // POST    /insertFestival                               controllers.Application.insertFestival
    "POST /insertFestival " in new WithApplication(fakeApp) {

      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser)

      // --- 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/insertFestival").withFormUrlEncodedBody(
         "festivalName" -> "TEST_FESTIVAL_NAME_XXXXXXXXXXXXXXXXXXXXX"
        ,"description"  -> "TEST_FESTIVAL_DESCRIPTION"
        ,"stageName[0]" -> "TEST_STAGE_NAME_1"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get

      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)

      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/insertFestival").withFormUrlEncodedBody(
         "festivalName" -> "TEST_FESTIVAL_NAME"
        ,"description"  -> "TEST_FESTIVAL_DESCRIPTION"
        ,"stageName[0]" -> "TEST_STAGE_NAME_1"
        ,"stageName[1]" -> "TEST_STAGE_NAME_2"
        ,"stageName[2]" -> "TEST_STAGE_NAME_3"
        ,"stageName[3]" -> "TEST_STAGE_NAME_4"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 登録されたFestivalを取得
      val festival: Option[Festival] = Festival.findById(1)
      // 登録されていること
      festival must beSome[Festival]
      // 項目の確認
      festival.get.festivalName must beMatching("TEST_FESTIVAL_NAME")
      festival.get.description  must beMatching("TEST_FESTIVAL_DESCRIPTION")

      // 登録されたStageを取得
      val stageList: Seq[Stage] = Stage.findByFestivalId(1)
      // 件数確認
      stageList must have size(4)
      // stageName確認（固定で確認）
      stageList(0).stageName must beMatching("TEST_STAGE_NAME_1")
      stageList(1).stageName must beMatching("TEST_STAGE_NAME_2")
      stageList(2).stageName must beMatching("TEST_STAGE_NAME_3")
      stageList(3).stageName must beMatching("TEST_STAGE_NAME_4")
    }

    // GET     /editFestival/:festivalId                     controllers.Application.editFestival(festivalId: Long)
    "GET  /editFestival/:festivalId " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/editFestival/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      status(resultRoute) must equalTo(OK)
    }

    // POST    /updateFestival                               controllers.Application.updateFestival(festivalId: Long)
    "POST /updateFestival " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // --- 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/updateFestival?festivalId=900001").withFormUrlEncodedBody(
         "festivalName" -> "TEST_FESTIVAL_NAME_XXXXXXXXXXXXXXXXXXXXX"
        ,"description"  -> "TEST_FESTIVAL_DESCRIPTION_UPDATED_UPDATE"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/updateFestival?festivalId=900001").withFormUrlEncodedBody(
         "festivalName" -> "TEST_UPDATED_FES"
        ,"description"  -> "TEST_FESTIVAL_DESCRIPTION_UPDATED_UPDATE"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 更新されたFestivalを取得
      val festival: Option[Festival] = Festival.findById(900001)
      // Festivalが取得できる事
      festival must beSome[Festival]
      // 項目の確認
      festival.get.festivalName must beMatching("TEST_UPDATED_FES")
      festival.get.description  must beMatching("TEST_FESTIVAL_DESCRIPTION_UPDATED_UPDATE")
    }

    // GET     /deleteFestival/:festivalId                   controllers.Application.deleteFestival(festivalId: Long)
    "GET  /deleteFestival/:festivalId " in new WithApplication(fakeApp) {

      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)

      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/deleteFestival/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // Festivalを取得
      val festival: Option[Festival] = Festival.findById(900001)
      // Festivalが登録されていないこと
      festival must beNone
    }

    // # Stage
    // GET     /createStage/:festivalId                      controllers.Application.createStage(festivalId: Long)
    "GET  /createStage/:festivalId " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/createStage/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      status(resultRoute) must equalTo(OK)
    }

    // POST    /insertStage                                  controllers.Application.insertStage(festivalId: Long)
    "POST /insertStage " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)

      // 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/insertStage?festivalId=900001").withFormUrlEncodedBody(
        "stageName" -> "TEST_STAGE_NAME_XXXXX"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)

      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/insertStage?festivalId=900001").withFormUrlEncodedBody(
        "stageName" -> "TEST_STAGE_NAME"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 登録されたStageを取得する
      val stage: Option[Stage] = Stage.findById(1)
      // 登録されていること
      stage must beSome[Stage]
      // 項目の確認
      stage.get.stageName must beMatching("TEST_STAGE_NAME")
    }

    // GET     /editStage/:festivalId/:stageId               controllers.Application.editStage(festivalId: Long, stageId: Long)
    "GET  /editStage/:festivalId/:stageId "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)

      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/editStage/900001/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
    }

    // POST    /updateStage                                  controllers.Application.updateStage(festivalId: Long, stageId: Long)
    "POST /updateStage "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)

      // 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/updateStage?festivalId=900001&stageId=900001").withFormUrlEncodedBody(
         "stageName" -> "TEST_STAGE_NAME_UPDATE_XXXXXXXX"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get

      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)

      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/updateStage?festivalId=900001&stageId=900001").withFormUrlEncodedBody(
         "stageName" -> "TEST_STAGE_NAME_UP"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 更新されたStageを取得
      val stage: Option[Stage] = Stage.findById(900001)
      // Stageが取得できる事
      stage must beSome[Stage]
      // 項目の確認
      stage.get.stageName must beMatching("TEST_STAGE_NAME_UP")
    }

    // GET     /deleteStage/:festivalId/:stageId             controllers.Application.deleteStage(festivalId: Long, stageId: Long)
    "GET  /deleteStage/:festivalId/:stageId "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)

      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/deleteStage/900001/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // Stageを取得
      val stage: Option[Stage] = Stage.findById(900001)
      // Festivalが登録されていないこと
      stage must beNone
    }

    // # Performance
    // GET     /createPerformance/:festivalId                controllers.Application.createPerformance(festivalId: Long)
    "GET  /createPerformance/:festivalId " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/createPerformance/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      status(resultRoute) must equalTo(OK)
    }
    // POST    /insertPerformance                            controllers.Application.insertPerformance(festivalId: Long)
    "POST /insertPerformance " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)

      // 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/insertPerformance?festivalId=900001").withFormUrlEncodedBody(
         "festivalId" -> "900001"
        ,"stageId"    -> "900001"
        ,"artist"     -> "TEST_ARTIST_NAME_XXXXXXXXXXXXXXXXXXX"
        ,"time"       -> "10:00"
        ,"timeFrame"  -> "30"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)

      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/insertPerformance?festivalId=900001").withFormUrlEncodedBody(
         "festivalId" -> "900001"
        ,"stageId"    -> "900001"
        ,"artist"     -> "TEST_ARTIST_NAME"
        ,"time"       -> "10:00"
        ,"timeFrame"  -> "30"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 登録されたPerformanceを取得する
      val performance: Option[Performance] = Performance.findById(1)
      // 登録されていること
      performance must beSome[Performance]
      // 項目の確認
      performance.get.festivalId must beEqualTo(900001)
      performance.get.stageId    must beEqualTo(900001)
      performance.get.artist     must beMatching("TEST_ARTIST_NAME")
      performance.get.time       must beMatching("10:00")
      performance.get.timeFrame  must beMatching("30")
    }

    // GET     /editPerformance/:festivalId/:performanceId   controllers.Application.editPerformance(festivalId: Long, performanceId: Long)
    "GET  /editPerformance/:festivalId/:performanceId "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)

      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/editPerformance/900001/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
    }

    // POST    /updatePerformance                            controllers.Application.updatePerformance(festivalId: Long, performanceId: Long)
    "POST /updatePerformance "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/updatePerformance?festivalId=900001&performanceId=900001").withFormUrlEncodedBody(
         "festivalId" -> "900001"
        ,"stageId"    -> "900001"
        ,"artist"     -> "TEST_ARTIST_NAME_UPDATE_XXXXXXXXXXXX"
        ,"time"       -> "10:30"
        ,"timeFrame"  -> "60"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/updatePerformance?festivalId=900001&performanceId=900001").withFormUrlEncodedBody(
         "festivalId" -> "900001"
        ,"stageId"    -> "900001"
        ,"artist"     -> "TEST_ARTIST_NAME_UPDATE"
        ,"time"       -> "10:30"
        ,"timeFrame"  -> "60"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 更新されたPerformanceを取得
      val performance: Option[Performance] = Performance.findById(900001)
      // Performanceが取得できる事
      performance must beSome[Performance]
      // 項目の確認
      performance.get.artist    must beMatching("TEST_ARTIST_NAME_UPDATE")
      performance.get.time      must beMatching("10:30")
      performance.get.timeFrame must beMatching("60")
    }

    // GET     /deletePerformance/:festivalId/:performanceId controllers.Application.deletePerformance(festivalId: Long, performanceId: Long)
    "GET  /deletePerformance/:festivalId/:performanceId "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/deletePerformance/900001/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // Performanceを取得
      val performance: Option[Performance] = Performance.findById(900001)
      // Performanceが登録されていないこと
      performance must beNone
    }

    // # TimeTable
    // GET     /timetable/:targetTwitterId/:festivalId       controllers.Application.timetable(targetTwitterId: Long, festivalId: Long)
    "GET  /timetable/:targetTwitterId/:festivalId "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance, createTestDataHeart)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/deletePerformance/900001/900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
    }

    // // # Twitter
    // // GET     /twitterLogin                                 controllers.TwitterController.twitterLogin
    // "GET  /twitterLogin " in new WithApplication(fakeApp) {
    //   // --- Database初期化
    //   executeDdl(createTableTwitterUser)
    //   // テスト対象実行
    //   val resultRoute = route(FakeRequest(GET, "/twitterLogin")).get
    //   status(resultRoute) must equalTo(SEE_OTHER)
    // }
    // 
    // // GET     /twitterOAuthCallback                         controllers.TwitterController.twitterOAuthCallback
    // "GET  /twitterOAuthCallback " in new WithApplication(fakeApp) {
    //   // --- Database初期化
    //   executeDdl(createTableTwitterUser)
    //   // 異常ケース
    //   val resultRouteByBadRequest = route(FakeRequest(GET, "/twitterOAuthCallback?denied=denied")).get
    //   status(resultRouteByBadRequest) must equalTo(SEE_OTHER)
    //   // テスト対象実行
    //   val resultRoute = route(FakeRequest(GET, "/twitterOAuthCallback?oauth_token=TEST_OAUTH_TOKEN&oauth_verifier=TEST_OAUTH_VERIRIER")).get
    //   status(resultRoute) must equalTo(SEE_OTHER)
    //   val twitterUser: Option[TwitterUser] = TwitterUser.findById(1)
    //   // TwitterUserが取得出来ること
    //   twitterUser must beSome[TwitterUser]
    // }

    // GET     /twitterLogout                                controllers.TwitterController.twitterLogout
    "GET  /twitterLogout" in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/twitterLogout").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      status(resultRoute) must equalTo(SEE_OTHER)
    }

    // # javascriptRoutes
    // GET /javascriptRoutes controllers.JsRouter.javascriptRoutes
    "GET  /javascriptRoutes" in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/javascriptRoutes")).get
      status(resultRoute) must equalTo(OK)
    }

    // # Ajax Festival
    // POST /ajaxUpdateFestival controllers.AjaxController.ajaxUpdateFestival(festivalId: Long, festivalName: String)
    "POST /ajaxUpdateFestival " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // --- 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajaxUpdateFestival?festivalId=900001&festivalName=TEST_FESTIVAL_NAME_XXXXXXXXXXXXXXXXXXXXX").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajaxUpdateFestival?festivalId=900001&festivalName=TEST_UPDATED_FES").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたFestivalを取得
      val festival: Option[Festival] = Festival.findById(900001)
      // Festivalが取得できる事
      festival must beSome[Festival]
      // 項目の確認
      festival.get.festivalName must beMatching("TEST_UPDATED_FES")
    }

    // # Ajax Stage
    // POST    /ajaxUpdateStage                              controllers.AjaxController.ajaxUpdateStage(stageId: Long, stageName: String)
    "POST /ajaxUpdateStage "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)
      // 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajaxUpdateStage?stageId=900001&stageName=TEST_STAGE_NAME_XXXXXXXX").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajaxUpdateStage?stageId=900001&stageName=TEST_STAGE_NAME_UP").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたStageを取得
      val stage: Option[Stage] = Stage.findById(900001)
      // Stageが取得できる事
      stage must beSome[Stage]
      // 項目の確認
      stage.get.stageName must beMatching("TEST_STAGE_NAME_UP")
    }

    // POST /ajaxUpdatePerformance controllers.AjaxController.ajaxUpdatePerformance(performanceId: Long, artist: String)
    "POST /ajaxUpdatePerformance "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajaxUpdatePerformance?performanceId=900001&artist=TEST_ARTIST_NAME_UPDATE_XXXXXXXXXXXX").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajaxUpdatePerformance?performanceId=900001&artist=TEST_ARTIST_NAME_UPDATE").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたPerformanceを取得
      val performance: Option[Performance] = Performance.findById(900001)
      // Performanceが取得できる事
      performance must beSome[Performance]
      // 項目の確認
      performance.get.artist must beMatching("TEST_ARTIST_NAME_UPDATE")
    }

    // POST /ajaxUpdatePerformanceByTimeFrame controllers.AjaxController.ajaxUpdatePerformanceByTimeFrame(performanceId: Long, stageId: Long, time: String)
    "POST /ajaxUpdatePerformanceByTimeFrame "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajaxUpdatePerformanceByTimeFrame?performanceId=900002&stageId=900001&time=10:30").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajaxUpdatePerformanceByTimeFrame?performanceId=900001&stageId=900001&time=10:30").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたPerformanceを取得
      val performance: Option[Performance] = Performance.findById(900001)
      // Performanceが取得できる事
      performance must beSome[Performance]
      // 項目の確認
      performance.get.time must beMatching("10:30")
    }

    // # Ajax Heart
    // POST /ajaxInsertHeart controllers.AjaxController.ajaxInsertHeart(festivalId: Long)
    "POST /ajaxInsertHeart "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // 異常ケース
      // テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajaxInsertHeart?festivalId=900002").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // 正常ケース
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajaxInsertHeart?festivalId=900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたHeartを取得
      val heart: Option[Heart] = Heart.findById(1)
      // Heartが取得できる事
      heart must beSome[Heart]
    }

    // POST /ajaxDeleteHeart controllers.AjaxController.ajaxDeleteHeart(festivalId: Long)
    "POST /ajaxDeleteHeart "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataHeart)
      // テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajaxDeleteHeart?festivalId=900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN", "accessTokenSecret" -> "ACCESS_TOKEN_SECRET")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたHeartを取得
      val heart: Option[Heart] = Heart.findById(900001)
      // Heartが取得できない事
      heart must beNone
    }

    // GET /withdraw controllers.Application.withdraw
    "GET  /withdraw "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/withdraw").withSession("twitterId" -> "900001")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
    }

    // GET /deleteAll controllers.Application.deleteAll
    "GET  /deleteAll "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableHeart, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataHeart, createTestDataStage, createTestDataPerformance)
      // テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/deleteAll").withSession("twitterId" -> "900001")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 削除されたデータを取得し、存在しないことを確認
      val twitterUser: Option[TwitterUser] = TwitterUser.findById(900001)
      twitterUser must beNone
      val festival:    Option[Festival]    = Festival.findById(900001)
      festival must beNone
      val heart:       Option[Heart]       = Heart.findById(900001)
      heart must beNone
      val stage:       Option[Stage]       = Stage.findById(900001)
      stage must beNone
      val performance: Option[Performance] = Performance.findById(900001)
      performance must beNone
    }
  }

  /********************************************************
   * テーブル登録処理
   ********************************************************/
  def executeDdl(sqlList: String*): Unit = {
    var executeSql: String = ""
    sqlList.foreach { sql =>
      executeSql = executeSql.union(sql)
    }
    DB.withConnection { implicit connection =>
      SQL(executeSql).executeUpdate()
    }
  }
  /** TwitetrUser */
  val createTableTwitterUser = 
  """
    create table twitter_user (
      id                          bigint auto_increment not null,
      twitter_id                  bigint UNIQUE,
      twitter_name                varchar(255),
      twitter_screen_name         varchar(255),
      twitter_profiel_image_url   varchar(255),
      twitter_description         text,
      twitter_access_token        varchar(255),
      twitter_access_token_secret varchar(255),
      create_date                 datetime not null,
      update_date                 datetime not null)
    ;
  """
  /** Festival */
  val createTableFestival = 
  """
    create table festival (
      id                        bigint auto_increment not null,
      festival_name             varchar(255),
      twitter_id                bigint,
      description               longtext,
      create_date               datetime not null,
      update_date               datetime not null)
    ;
  """
  /** Festival */
  val createTableStage = 
  """
    create table stage (
      id                        bigint auto_increment not null,
      festival_id               bigint,
      stage_name                varchar(255),
      sort                      varchar(255),
      color                     varchar(255),
      create_date               datetime not null,
      update_date               datetime not null)
    ;
  """

  /** Performance */
  val createTablePerformance = 
  """
    create table performance (
      id                        bigint auto_increment not null,
      festival_id               bigint,
      stage_id                  bigint,
      artist                    varchar(255),
      time                      varchar(255),
      time_frame                varchar(255),
      create_date               datetime not null,
      update_date               datetime not null)
    ;
  """

  /** Heart */
  val createTableHeart = 
  """
    create table heart (
      id                        bigint auto_increment not null,
      festival_id               bigint,
      twitter_id                bigint,
      create_date               datetime not null,
      update_date               datetime not null)
    ;
  """

  /********************************************************
   * テストデータ作成処理
   ********************************************************/
  def createTestData(simpleSqlList: SimpleSql[anorm.Row]*): Unit = {
    DB.withConnection { implicit connection =>
      simpleSqlList.foreach { simpleSql =>
        simpleSql.executeUpdate()
      }
    }
  }

  /** TwitterUser */
  def createTestDataTwitterUser = {
    val twitterUserParams = Seq[NamedParameter](
         'id                          -> Option(900001)
        ,'twitter_id                  -> 900001
        ,'twitter_name                -> "TEST_USER_NAME"
        ,'twitter_screen_name         -> "TEST_USER_SCREEN_NAME"
        ,'twitter_profiel_image_url   -> "https://localhost:9000/image.jpg"
        ,'twitter_description         -> "Description"
        ,'twitter_access_token        -> "ACCESS_TOKEN"
        ,'twitter_access_token_secret -> "ACCESS_TOKEN_SECRET"
        ,'create_date                 -> Some(nowDate)
        ,'update_date                 -> Some(nowDate)
    )
    SQL(
      """
        insert into twitter_user(
           id
          ,twitter_id
          ,twitter_name
          ,twitter_screen_name
          ,twitter_profiel_image_url
          ,twitter_description
          ,twitter_access_token
          ,twitter_access_token_secret
          ,create_date
          ,update_date
        ) values (
           {id}
          ,{twitter_id}
          ,{twitter_name}
          ,{twitter_screen_name}
          ,{twitter_profiel_image_url}
          ,{twitter_description}
          ,{twitter_access_token}
          ,{twitter_access_token_secret}
          ,{create_date}
          ,{update_date}
        )
      """
    ).on(
      twitterUserParams: _*
    )
  }

  /** Festival */
  def createTestDataFestival = {
    val festvalParams = Seq[NamedParameter](
       'id            -> Option(900001)
      ,'festival_name -> "TEST_FESTIVAL_NAME"
      ,'twitter_id    -> 900001
      ,'description   -> "TEST_FESTIVAL_DESCRIPTION"
      ,'create_date   -> Some(nowDate)
      ,'update_date   -> Some(nowDate)
    )
    SQL(
      """
        insert into festival(
           id
          ,twitter_id
          ,festival_name
          ,description
          ,create_date
          ,update_date
        ) values (
           {id}
          ,{twitter_id}
          ,{festival_name}
          ,{description}
          ,{create_date}
          ,{update_date}
        )
      """
    ).on(
      festvalParams: _*
    )
  }

  /** Stage */
  def createTestDataStage = {
    val stageParams = Seq[NamedParameter](
       'id          -> Option(900001)
      ,'festival_id -> 900001
      ,'stage_name  -> "TEST_STAGE_NAME"
      ,'sort        -> Option(100)
      ,'color       -> Option("white")
      ,'create_date -> Some(nowDate)
      ,'update_date -> Some(nowDate)
    )
    SQL(
      """
        insert into stage(
           id
          ,festival_id
          ,stage_name
          ,sort
          ,color
          ,create_date
          ,update_date
        ) values (
           {id}
          ,{festival_id}
          ,{stage_name}
          ,{sort}
          ,{color}
          ,{create_date}
          ,{update_date}
        )
      """
    ).on(
      stageParams: _*
    )
  }

  /** Performance */
  def createTestDataPerformance = {
    val performanceParams = Seq[NamedParameter](
       'id          -> Option(900001)
      ,'festival_id -> 900001
      ,'stage_id    -> 900001
      ,'artist      -> "TEST_ARTIST_NAME"
      ,'time        -> "10:00"
      ,'time_frame  -> "30"
      ,'create_date -> Some(nowDate)
      ,'update_date -> Some(nowDate)
    )
    SQL(
      """
        insert into performance(
           id
          ,festival_id
          ,stage_id
          ,artist
          ,time
          ,time_frame
          ,create_date
          ,update_date
        ) values (
           {id}
          ,{festival_id}
          ,{stage_id}
          ,{artist}
          ,{time}
          ,{time_frame}
          ,{create_date}
          ,{update_date}
        )
      """
    ).on(
      performanceParams: _*
    )
  }

  /** Heart */
  def createTestDataHeart = {
    val heartParams = Seq[NamedParameter](
       'id          -> Option(900001)
      ,'festival_id -> 900001
      ,'twitter_id  -> 900001
      ,'create_date -> Some(nowDate)
      ,'update_date -> Some(nowDate)
    )
    SQL(
      """
        insert into heart(
           id
          ,festival_id
          ,twitter_id
          ,create_date
          ,update_date
        ) values (
           {id}
          ,{festival_id}
          ,{twitter_id}
          ,{create_date}
          ,{update_date}
        )
      """
    ).on(
      heartParams: _*
    )
  }

  /** 共通項目 */
  def nowDate: java.util.Date = new java.util.Date
}
