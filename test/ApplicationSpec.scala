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
    
    // ----------------------------------------------------------------------------------------------
    // Static
    // ----------------------------------------------------------------------------------------------
    // GET  /hoge send 404 on a bad request
    "GET  /hoge/hogehoge " in new WithApplication(fakeApp) {
      route(FakeRequest(GET, "/hoge/foo")) must beNone
    }

    // GET  / controllers.Application.index(p: Int ?= 1)
    "GET  / " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableHeart)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // GET  /usage controllers.Application.usage
    "GET  /mytite/usage " in new WithApplication(fakeApp) {
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/mytite/usage")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // GET  /about controllers.Application.about
    "GET  /mytite/about " in new WithApplication(fakeApp) {
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/mytite/about")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // GET /withdraw controllers.Application.withdraw
    "GET  /mytite/withdraw "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/mytite/withdraw").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
    }

    // POST /mytite/d controllers.Application.deleteAll
    "POST /mytite/d "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableHeart, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataHeart, createTestDataStage, createTestDataPerformance)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/mytite/d").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
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

    // ----------------------------------------------------------------------------------------------
    // Festival
    // ----------------------------------------------------------------------------------------------
    // GET  /:twitterScreenName controllers.Application.festival(p: Int ?= 1, twitterScreenName: String)
    "GET  /:twitterScreenName " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/TEST_USER_SCREEN_NAME")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // GET  /:twitterScreenName/c controllers.Application.createFestival
    "GET  /:twitterScreenName/c " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/TEST_USER_SCREEN_NAME/c").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      status(resultRoute) must equalTo(OK)
      contentType(resultRoute) must beSome.which(_ == "text/html")
    }

    // POST /:twitterScreenName/i controllers.Application.insertFestival
    "POST /:twitterScreenName/i " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser)
      // --- 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/TEST_USER_SCREEN_NAME/i").withFormUrlEncodedBody(
         "festivalName" -> "TEST_FESTIVAL_NAME_XXXXXXXXXXXXXXXXXXXXX"
        ,"description"  -> "TEST_FESTIVAL_DESCRIPTION"
        ,"stageName[0]" -> "TEST_STAGE_NAME_1"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/TEST_USER_SCREEN_NAME/i").withFormUrlEncodedBody(
         "festivalName" -> "TEST_FESTIVAL_NAME"
        ,"description"  -> "TEST_FESTIVAL_DESCRIPTION"
        ,"stageName[0]" -> "TEST_STAGE_NAME_1"
        ,"stageName[1]" -> "TEST_STAGE_NAME_2"
        ,"stageName[2]" -> "TEST_STAGE_NAME_3"
        ,"stageName[3]" -> "TEST_STAGE_NAME_4"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
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

    // GET  /:festivalId/fes/e controllers.Application.editFestival(festivalId: Long)
    "GET  /:festivalId/fes/e " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/900001/fes/e").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      status(resultRoute) must equalTo(OK)
    }

    // POST /:festivalId/fes/u controllers.Application.updateFestival(festivalId: Long)
    "POST /:festivalId/fes/u " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // --- 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/900001/fes/u").withFormUrlEncodedBody(
         "festivalName" -> "TEST_FESTIVAL_NAME_XXXXXXXXXXXXXXXXXXXXX"
        ,"description"  -> "TEST_FESTIVAL_DESCRIPTION_UPDATED_UPDATE"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/900001/fes/u").withFormUrlEncodedBody(
         "festivalName" -> "TEST_UPDATED_FES"
        ,"description"  -> "TEST_FESTIVAL_DESCRIPTION_UPDATED_UPDATE"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
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

    // POST /:festivalId/fes/d controllers.Application.deleteFestival(festivalId: Long)
    "POST /:festivalId/fes/d " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/900001/fes/d").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // Festivalを取得
      val festival: Option[Festival] = Festival.findById(900001)
      // Festivalが登録されていないこと
      festival must beNone
    }

    // ----------------------------------------------------------------------------------------------
    // Stage
    // ----------------------------------------------------------------------------------------------
    // GET  /:festivalId/sta/c         controllers.Application.createStage(festivalId: Long)
    "GET  /:festivalId/sta/c " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/900001/sta/c").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      status(resultRoute) must equalTo(OK)
    }

    // POST /:festivalId/sta/i         controllers.Application.insertStage(festivalId: Long)
    "POST /:festivalId/sta/i " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/900001/sta/i").withFormUrlEncodedBody(
        "festivalId" -> "900001"
        ,"stageName" -> "TEST_STAGE_NAME_XXXXX"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/900001/sta/i").withFormUrlEncodedBody(
         "festivalId" -> "900001"
        ,"stageName"  -> "TEST_STAGE_NAME"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 登録されたStageを取得する
      val stage: Option[Stage] = Stage.findById(1)
      // 登録されていること
      stage must beSome[Stage]
      // 項目の確認
      stage.get.stageName must beMatching("TEST_STAGE_NAME")
    }

    // GET  /:festivalId/:stageId/sta/e  controllers.Application.editStage(festivalId: Long, stageId: Long)
    "GET  /:festivalId/:stageId/sta/e "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/900001/900001/sta/e").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
    }

    // POST /:festivalId/:stageId/sta/u  controllers.Application.updateStage(festivalId: Long, stageId: Long)
    "POST /:festivalId/:stageId/sta/u "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)
      // 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/900001/900001/sta/u").withFormUrlEncodedBody(
         "festivalId"     -> "900001"
        ,"stageName" -> "TEST_STAGE_NAME_UPDATE_XXXXXXXX"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/900001/900001/sta/u").withFormUrlEncodedBody(
         "festivalId"     -> "900001"
        ,"stageName" -> "TEST_STAGE_NAME_UP"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // 更新されたStageを取得
      val stage: Option[Stage] = Stage.findById(900001)
      // Stageが取得できる事
      stage must beSome[Stage]
      // 項目の確認
      stage.get.stageName must beMatching("TEST_STAGE_NAME_UP")
    }

    // POST /:festivalId/:stageId/sta/d  controllers.Application.deleteStage(festivalId: Long, stageId: Long)
    "POST /:festivalId/:stageId/sta/d "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/900001/900001/sta/d").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // Stageを取得
      val stage: Option[Stage] = Stage.findById(900001)
      // Festivalが登録されていないこと
      stage must beNone
    }

    // ----------------------------------------------------------------------------------------------
    // Performance
    // ----------------------------------------------------------------------------------------------
    // GET  /:festivalId/per/c controllers.Application.createPerformance(festivalId: Long)
    "GET  /:festivalId/per/c " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/900001/per/c").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      status(resultRoute) must equalTo(OK)
    }
    // POST /:festivalId/per/i controllers.Application.insertPerformance(festivalId: Long)
    "POST /:festivalId/per/i " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)
      // 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/900001/per/i").withFormUrlEncodedBody(
         "festivalId"     -> "900001"
        ,"stageId"     -> "900001"
        ,"artist"    -> "TEST_ARTIST_NAME_XXXXXXXXXXXXXXXXXXX"
        ,"time"      -> "10:00"
        ,"timeFrame" -> "30"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/900001/per/i").withFormUrlEncodedBody(
         "festivalId"     -> "900001"
        ,"stageId"     -> "900001"
        ,"artist"    -> "TEST_ARTIST_NAME"
        ,"time"      -> "10:00"
        ,"timeFrame" -> "30"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
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
    // GET  /:festivalId/:performanceId/per/e  controllers.Application.editPerformance(festivalId: Long, performanceId: Long)
    "GET  /:festivalId/:performanceId/per/e "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/900001/900001/per/e").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
    }
    // POST /:festivalId/:performanceId/per/u controllers.Application.updatePerformance(festivalId: Long, performanceId: Long)
    "POST /:festivalId/:performanceId/per/u "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/900001/900001/per/u").withFormUrlEncodedBody(
         "festivalId"     -> "900001"
        ,"stageId"     -> "900001"
        ,"artist"    -> "TEST_ARTIST_NAME_UPDATE_XXXXXXXXXXXX"
        ,"time"      -> "10:30"
        ,"timeFrame" -> "60"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/900001/900001/per/u").withFormUrlEncodedBody(
         "festivalId"     -> "900001"
        ,"stageId"     -> "900001"
        ,"artist"    -> "TEST_ARTIST_NAME_UPDATE"
        ,"time"      -> "10:30"
        ,"timeFrame" -> "60"
      ).withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
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
    // POST /:festivalId/:performanceId/per/d  controllers.Application.deletePerformance(festivalId: Long, performanceId: Long)
    "POST /:festivalId/:performanceId/per/d "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/900001/900001/per/d").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(SEE_OTHER)
      // Performanceを取得
      val performance: Option[Performance] = Performance.findById(900001)
      // Performanceが登録されていないこと
      performance must beNone
    }

    // ----------------------------------------------------------------------------------------------
    // # TimeTable
    // ----------------------------------------------------------------------------------------------
    // GET  /:twitterScreenName/:festivalId/ controllers.Application.timetable(twiId: Long, festivalId: Long)
    "GET  /:twitterScreenName/:festivalId/ "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance, createTestDataHeart)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/TEST_USER_SCREEN_NAME/900001/").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
    }

    // ----------------------------------------------------------------------------------------------
    // # Twitter
    // ----------------------------------------------------------------------------------------------
    // // GET /twitterLogin controllers.TwitterController.twitterLogin
    // "GET  /twitterLogin " in new WithApplication(fakeApp) {
    //   // --- Database初期化
    //   executeDdl(createTableTwitterUser)
    //   // --- テスト対象実行
    //   val resultRoute = route(FakeRequest(GET, "/twitterLogin")).get
    //   status(resultRoute) must equalTo(SEE_OTHER)
    // }
    // 
    // // GET  /twitterOAuthCallback controllers.TwitterController.twitterOAuthCallback
    // "GET  /twitterOAuthCallback " in new WithApplication(fakeApp) {
    //   // --- Database初期化
    //   executeDdl(createTableTwitterUser)
    //   // 異常ケース
    //   val resultRouteByBadRequest = route(FakeRequest(GET, "/twitterOAuthCallback?denied=denied")).get
    //   status(resultRouteByBadRequest) must equalTo(SEE_OTHER)
    //   // --- テスト対象実行
    //   val resultRoute = route(FakeRequest(GET, "/twitterOAuthCallback?oauth_token=TEST_OAUTH_TOKEN&oauth_verifier=TEST_OAUTH_VERIRIER")).get
    //   status(resultRoute) must equalTo(SEE_OTHER)
    //   val twitterUser: Option[TwitterUser] = TwitterUser.findById(1)
    //   // TwitterUserが取得出来ること
    //   twitterUser must beSome[TwitterUser]
    // }
    // GET  /twitter/Logout controllers.TwitterController.twitterLogout
    "GET  /twitter/Logout" in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/twitter/Logout").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      status(resultRoute) must equalTo(SEE_OTHER)
    }

    // ----------------------------------------------------------------------------------------------
    // # JavaScript Ajax
    // ----------------------------------------------------------------------------------------------
    // GET /javascript/Routes controllers.JsRouter.javascriptRoutes
    "GET  /javascript/Routes" in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(GET, "/javascript/Routes")).get
      status(resultRoute) must equalTo(OK)
    }

    // POST /ajax/UpdateFestival controllers.AjaxController.ajaxUpdateFestival(festivalId: Long, festivalName: String)
    "POST /ajax/UpdateFestival " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival)
      // --- 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajax/UpdateFestival?festivalId=900001&festivalName=TEST_FESTIVAL_NAME_XXXXXXXXXXXXXXXXXXXXX").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajax/UpdateFestival?festivalId=900001&festivalName=TEST_UPDATED_FES").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたFestivalを取得
      val festival: Option[Festival] = Festival.findById(900001)
      // Festivalが取得できる事
      festival must beSome[Festival]
      // 項目の確認
      festival.get.festivalName must beMatching("TEST_UPDATED_FES")
    }

    // POST /ajax/UpdateStage controllers.AjaxController.ajaxUpdateStage(stageId: Long, stageName: String)
    "POST /ajax/UpdateStage "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage)
      // 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajax/UpdateStage?stageId=900001&stageName=TEST_STAGE_NAME_XXXXXXXX").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajax/UpdateStage?stageId=900001&stageName=TEST_STAGE_NAME_UP").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたStageを取得
      val stage: Option[Stage] = Stage.findById(900001)
      // Stageが取得できる事
      stage must beSome[Stage]
      // 項目の確認
      stage.get.stageName must beMatching("TEST_STAGE_NAME_UP")
    }

    // POST /ajax/UpdatePerformance controllers.AjaxController.ajaxUpdatePerformance(performanceId: Long, artist: String)
    "POST /ajax/UpdatePerformance " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajax/UpdatePerformance?performanceId=900001&artist=TEST_ARTIST_NAME_UPDATE_XXXXXXXXXXXX").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajax/UpdatePerformance?performanceId=900001&artist=TEST_ARTIST_NAME_UPDATE").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたPerformanceを取得
      val performance: Option[Performance] = Performance.findById(900001)
      // Performanceが取得できる事
      performance must beSome[Performance]
      // 項目の確認
      performance.get.artist must beMatching("TEST_ARTIST_NAME_UPDATE")
    }

    // POST /ajax/UpdatePerformanceByTimeFrame controllers.AjaxController.ajaxUpdatePerformanceByTimeFrame(performanceId: Long, stageId: Long, time: String)
    "POST /ajax/UpdatePerformanceByTimeFrame "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajax/UpdatePerformanceByTimeFrame?performanceId=900002&stageId=900001&time=10:30").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajax/UpdatePerformanceByTimeFrame?performanceId=900001&stageId=900001&time=10:30").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたPerformanceを取得
      val performance: Option[Performance] = Performance.findById(900001)
      // Performanceが取得できる事
      performance must beSome[Performance]
      // 項目の確認
      performance.get.time must beMatching("10:30")
    }

    // POST /ajax/InsertHeart controllers.AjaxController.ajaxInsertHeart(festivalId: Long)
    "POST /ajax/InsertHeart " in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableStage, createTablePerformance, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataStage, createTestDataPerformance)
      // 異常ケース
      // --- テスト対象実行
      val resultRouteByBadRequest = route(FakeRequest(POST, "/ajax/InsertHeart?festivalId=900002").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRouteByBadRequest) must equalTo(BAD_REQUEST)
      // --- 正常ケース
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajax/InsertHeart?festivalId=900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたHeartを取得
      val heart: Option[Heart] = Heart.findById(1)
      // Heartが取得できる事
      heart must beSome[Heart]
    }

    // POST /ajax/DeleteHeart controllers.AjaxController.ajaxDeleteHeart(festivalId: Long)
    "POST /ajax/DeleteHeart "  in new WithApplication(fakeApp) {
      // --- Database初期化
      executeDdl(createTableTwitterUser, createTableFestival, createTableHeart)
      // --- テストデータ作成
      createTestData(createTestDataTwitterUser, createTestDataFestival, createTestDataHeart)
      // --- テスト対象実行
      val resultRoute = route(FakeRequest(POST, "/ajax/DeleteHeart?festivalId=900001").withSession("twitterId" -> "900001", "accessToken" -> "ACCESS_TOKEN")).get
      // リターン値
      status(resultRoute) must equalTo(OK)
      // 更新されたHeartを取得
      val heart: Option[Heart] = Heart.findById(900001)
      // Heartが取得できない事
      heart must beNone
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
  val commonIdl: Long   = 900001
  val commonIds: String = "900001"
}
