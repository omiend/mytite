
package views.html

import play.templates._
import play.templates.TemplateMagic._

import play.api.templates._
import play.api.templates.PlayMagic._
import models._
import controllers._
import play.api.i18n._
import play.api.mvc._
import play.api.data._
import views.html._
/**/
object index extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template2[Pager[TwitterUser],play.api.mvc.Flash,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(pager: Pager[TwitterUser])(implicit flash: play.api.mvc.Flash):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.65*/("""

"""),_display_(Seq[Any](/*3.2*/main(pager.title, pager.twitterUser)/*3.38*/(flash)/*3.45*/ {_display_(Seq[Any](format.raw/*3.47*/("""

<div class="col-xs-12 col-sm-12">
  <p class="pull-right visible-xs">
    <button type="button" class="btn btn-primary btn-xs" data-toggle="offcanvas">Toggle nav</button>
  </p>
  <div class="jumbotron">
    <h1></h1>
    <p></p>
  </div>

  <div class="row">

    """),_display_(Seq[Any](/*16.6*/for(user <- pager.dataList) yield /*16.33*/{_display_(Seq[Any](format.raw/*16.34*/("""
      <div class="col-6 col-sm-6 col-lg-4">
        <h2><img src=""""),_display_(Seq[Any](/*18.24*/user/*18.28*/.twitterProfielImageUrl)),format.raw/*18.51*/("""" alt="Profiel Image" class="img-rounded"><a href="https://twitter.com/"""),_display_(Seq[Any](/*18.123*/user/*18.127*/.twitterScreenName)),format.raw/*18.145*/("""">&#64;"""),_display_(Seq[Any](/*18.153*/user/*18.157*/.twitterScreenName)),format.raw/*18.175*/("""</a></h2>
        <p>"""),_display_(Seq[Any](/*19.13*/user/*19.17*/.twitterDescription)),format.raw/*19.36*/("""</p>
        <p><a class="btn btn-default" href="#" role="button">View details &raquo;</a></p>
      </div><!--/span-->
    """)))})),format.raw/*22.6*/("""

  </div><!--/row-->
</div><!--/span-->

""")))})),format.raw/*27.2*/("""
"""))}
    }
    
    def render(pager:Pager[TwitterUser],flash:play.api.mvc.Flash): play.api.templates.HtmlFormat.Appendable = apply(pager)(flash)
    
    def f:((Pager[TwitterUser]) => (play.api.mvc.Flash) => play.api.templates.HtmlFormat.Appendable) = (pager) => (flash) => apply(pager)(flash)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Thu Apr 03 22:11:09 JST 2014
                    SOURCE: /Users/omi-swordfish/work/play/tite_scala/app/views/index.scala.html
                    HASH: 2c754c59327c69b29881d06defe344a0e1167e05
                    MATRIX: 587->1|744->64|781->67|825->103|840->110|879->112|1182->380|1225->407|1264->408|1368->476|1381->480|1426->503|1535->575|1549->579|1590->597|1635->605|1649->609|1690->627|1748->649|1761->653|1802->672|1958->797|2032->840
                    LINES: 19->1|22->1|24->3|24->3|24->3|24->3|37->16|37->16|37->16|39->18|39->18|39->18|39->18|39->18|39->18|39->18|39->18|39->18|40->19|40->19|40->19|43->22|48->27
                    -- GENERATED --
                */
            