
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
object main extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template4[String,Option[TwitterUser],play.api.mvc.Flash,Html,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(title: String, twitterUser: Option[TwitterUser])(flash: play.api.mvc.Flash)(content: Html):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.93*/("""

<!DOCTYPE html>

<html>
  <head>
    <title>"""),_display_(Seq[Any](/*7.13*/title)),format.raw/*7.18*/(""" - マイタイテ！</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="shortcut icon" type="image/png" href=""""),_display_(Seq[Any](/*12.55*/routes/*12.61*/.Assets.at("images/favicon.png"))),format.raw/*12.93*/("""">
    <link rel="stylesheet" media="screen" href=""""),_display_(Seq[Any](/*13.50*/routes/*13.56*/.Assets.at("stylesheets/main.css"))),format.raw/*13.90*/("""">
    <link rel="stylesheet" media="screen" href=""""),_display_(Seq[Any](/*14.50*/routes/*14.56*/.Assets.at("bootstrap/css/bootstrap.css"))),format.raw/*14.97*/("""">
  </head>
  <body>

    <div class="navbar navbar-fixed-top navbar-inverse" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="/">マイタイテ！</a>
        </div>
        <div class="collapse navbar-collapse navbar-right">
          <ul class="nav navbar-nav">
            """),_display_(Seq[Any](/*31.14*/twitterUser/*31.25*/ match/*31.31*/ {/*32.15*/case Some(user) =>/*32.33*/ {_display_(Seq[Any](format.raw/*32.35*/("""
                <li><a href="https://twitter.com/"""),_display_(Seq[Any](/*33.51*/user/*33.55*/.twitterScreenName)),format.raw/*33.73*/("""">&#64;"""),_display_(Seq[Any](/*33.81*/user/*33.85*/.twitterScreenName)),format.raw/*33.103*/("""</a></li>
                <li><a href="/twitterLogout">Logout</a></li>
              """)))}/*36.15*/case _ =>/*36.24*/ {_display_(Seq[Any](format.raw/*36.26*/("""<li><a href="/twitterLogin">Login</a></li>""")))}})),format.raw/*37.14*/("""
          </ul>
        </div><!-- /.nav-collapse -->
      </div><!-- /.container -->
    </div><!-- /.navbar -->

    <div class="container">

      <div class="row row-offcanvas row-offcanvas-right">

        <div class="col-xs-12 col-sm-9">
            """),_display_(Seq[Any](/*48.14*/flash/*48.19*/.get("success").map/*48.38*/ { message =>_display_(Seq[Any](format.raw/*48.51*/(""" <div class="alert alert-success"><strong>Success!</strong>"""),_display_(Seq[Any](/*48.111*/message)),format.raw/*48.118*/("""</div> """)))})),format.raw/*48.126*/("""
            """),_display_(Seq[Any](/*49.14*/flash/*49.19*/.get("warning").map/*49.38*/ { message =>_display_(Seq[Any](format.raw/*49.51*/(""" <div class="alert alert-warning"><strong>Warning!</strong>"""),_display_(Seq[Any](/*49.111*/message)),format.raw/*49.118*/("""</div> """)))})),format.raw/*49.126*/("""
            """),_display_(Seq[Any](/*50.14*/flash/*50.19*/.get("error").map/*50.36*/ { message =>_display_(Seq[Any](format.raw/*50.49*/(""" <div class="alert alert-danger"><strong>Error!</strong>"""),_display_(Seq[Any](/*50.106*/message)),format.raw/*50.113*/("""</div> """)))})),format.raw/*50.121*/("""
            """),_display_(Seq[Any](/*51.14*/content)),format.raw/*51.21*/("""
        </div><!--/span-->

      </div><!--/row-->

      <hr>

      <footer>
        <p>&copy; omiend 2013</p>
      </footer>

    </div><!--/.container-->



    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src=""""),_display_(Seq[Any](/*69.42*/routes/*69.48*/.Assets.at("javascripts/jquery-1.9.0.min.js"))),format.raw/*69.93*/(""""></script>
    <script type="text/javascript" src=""""),_display_(Seq[Any](/*70.42*/routes/*70.48*/.Assets.at("bootstrap/js/bootstrap.min.js"))),format.raw/*70.91*/(""""></script>

  </body>
</html>
"""))}
    }
    
    def render(title:String,twitterUser:Option[TwitterUser],flash:play.api.mvc.Flash,content:Html): play.api.templates.HtmlFormat.Appendable = apply(title,twitterUser)(flash)(content)
    
    def f:((String,Option[TwitterUser]) => (play.api.mvc.Flash) => (Html) => play.api.templates.HtmlFormat.Appendable) = (title,twitterUser) => (flash) => (content) => apply(title,twitterUser)(flash)(content)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Tue Mar 25 18:38:04 JST 2014
                    SOURCE: /Users/omi-swordfish/work/play/tite_scala/app/views/main.scala.html
                    HASH: d619c3cccb561a2633b9686f599deff52edf967f
                    MATRIX: 599->1|784->92|866->139|892->144|1212->428|1227->434|1281->466|1369->518|1384->524|1440->558|1528->610|1543->616|1606->657|2303->1318|2323->1329|2338->1335|2349->1352|2376->1370|2416->1372|2503->1423|2516->1427|2556->1445|2600->1453|2613->1457|2654->1475|2759->1576|2777->1585|2817->1587|2893->1644|3188->1903|3202->1908|3230->1927|3281->1940|3378->2000|3408->2007|3449->2015|3499->2029|3513->2034|3541->2053|3592->2066|3689->2126|3719->2133|3760->2141|3810->2155|3824->2160|3850->2177|3901->2190|3995->2247|4025->2254|4066->2262|4116->2276|4145->2283|4540->2642|4555->2648|4622->2693|4711->2746|4726->2752|4791->2795
                    LINES: 19->1|22->1|28->7|28->7|33->12|33->12|33->12|34->13|34->13|34->13|35->14|35->14|35->14|52->31|52->31|52->31|52->32|52->32|52->32|53->33|53->33|53->33|53->33|53->33|53->33|55->36|55->36|55->36|55->37|66->48|66->48|66->48|66->48|66->48|66->48|66->48|67->49|67->49|67->49|67->49|67->49|67->49|67->49|68->50|68->50|68->50|68->50|68->50|68->50|68->50|69->51|69->51|87->69|87->69|87->69|88->70|88->70|88->70
                    -- GENERATED --
                */
            