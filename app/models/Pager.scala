package models

import scala.reflect.BeanProperty

/** ページング処理で利用するクラス */
case class Pager[A](var title: String, var pageNum: Int, var totalRows: Int, var twitterUser: TwitterUser, var dataList: Seq[A]) {

  /** １ページに表時するデータの件数 */
  val maxListCount: Int = 10

  /** 現在ページの両脇ページ番号を表示する件数 */
  val behindAndFrontCount: Int = 5

  /** 取得したデータの件数からページ数を計算 */
  lazy val maxPageRowCount: Int = {
    // ページ数を取得
    var maxPageRowCount: Int = totalRows / maxListCount
    if (totalRows % maxListCount > 0) {
      maxPageRowCount = maxPageRowCount + 1
    }
    maxPageRowCount
  }

  /** ページ番号リストを返却する */
  def pageNumList: Seq[Int] = {

    // 返却用
    var pageNumList: Seq[Int] = Seq.empty

    // ページ番号リスト作成起点を取得
    val startIndex: Int = {
      var ret: Int = 0
      if (pageNum + behindAndFrontCount + 1 > maxPageRowCount) {
        ret = maxPageRowCount - (behindAndFrontCount * 2)
      } else {
        ret = pageNum - behindAndFrontCount
      }
      if (ret <= 0) {
        ret = 1
      }
      ret
    }

    // ページ番号リストを作成
    for (i: Int <- startIndex to maxPageRowCount if pageNumList.size < (behindAndFrontCount * 2 + 1) && pageNumList.size < maxPageRowCount) {
      if (i >= 1 && maxPageRowCount >= i) {
        pageNumList = pageNumList :+ i
      }
      i + 1
    }
    pageNumList
  }
}
