
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

    """),_display_(Seq[Any](/*16.6*/for(twitterUser <- pager.dataList) yield /*16.40*/{_display_(Seq[Any](format.raw/*16.41*/("""
      <div class="col-6 col-sm-6 col-lg-4">
        <h2><img src=""""),_display_(Seq[Any](/*18.24*/twitterUser/*18.35*/.twitterProfielImageUrl)),format.raw/*18.58*/("""" alt="Profiel Image" class="img-rounded"><a href="https://twitter.com/"""),_display_(Seq[Any](/*18.130*/twitterUser/*18.141*/.twitterScreenName)),format.raw/*18.159*/("""">&#64;"""),_display_(Seq[Any](/*18.167*/twitterUser/*18.178*/.twitterScreenName)),format.raw/*18.196*/("""</a></h2>
        <p>音楽と映画と猫を...</p>
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
                    DATE: Sun Mar 23 12:26:41 JST 2014
                    SOURCE: /Users/omi-swordfish/work/play/tite_scala/app/views/index.scala.html
                    HASH: 5b7c6e3800a8bc37873c3e1e04c3e9a5a1ff56b8
                    MATRIX: 587->1|744->64|781->67|825->103|840->110|879->112|1182->380|1232->414|1271->415|1375->483|1395->494|1440->517|1549->589|1570->600|1611->618|1656->626|1677->637|1718->655|1906->812|1980->855
                    LINES: 19->1|22->1|24->3|24->3|24->3|24->3|37->16|37->16|37->16|39->18|39->18|39->18|39->18|39->18|39->18|39->18|39->18|39->18|43->22|48->27
                    -- GENERATED --
                */
            