
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
    def apply/*1.2*/(title: String, twitteUser: Option[TwitterUser])(flash: play.api.mvc.Flash)(content: Html):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.92*/("""

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
          """),_display_(Seq[Any](/*31.12*/twitteUser/*31.22*/ match/*31.28*/ {/*32.13*/case Some(user) =>/*32.31*/ {_display_(Seq[Any](format.raw/*32.33*/("""
              <li><a href="https://twitter.com/"""),_display_(Seq[Any](/*33.49*/user/*33.53*/.twitterScreenName)),format.raw/*33.71*/("""">&#64;"""),_display_(Seq[Any](/*33.79*/user/*33.83*/.twitterScreenName)),format.raw/*33.101*/("""</a></li>
              <li><a href="/twitterLogout">Logout</a></li>
            """)))}/*36.13*/case _ =>/*36.22*/ {_display_(Seq[Any](format.raw/*36.24*/("""
              <li><a href="/twitterLogin">Login</a></li>
            """)))}})),format.raw/*39.12*/("""
          </ul>
        </div><!-- /.nav-collapse -->
      </div><!-- /.container -->
    </div><!-- /.navbar -->

    <div class="container">

      <div class="row row-offcanvas row-offcanvas-right">

        <div class="col-xs-12 col-sm-12">
            """),_display_(Seq[Any](/*50.14*/flash/*50.19*/.get("success").map/*50.38*/ { message =>_display_(Seq[Any](format.raw/*50.51*/(""" <div class="alert alert-success"><strong>Success!</strong>"""),_display_(Seq[Any](/*50.111*/message)),format.raw/*50.118*/("""</div> """)))})),format.raw/*50.126*/("""
            """),_display_(Seq[Any](/*51.14*/flash/*51.19*/.get("warning").map/*51.38*/ { message =>_display_(Seq[Any](format.raw/*51.51*/(""" <div class="alert alert-warning"><strong>Warning!</strong>"""),_display_(Seq[Any](/*51.111*/message)),format.raw/*51.118*/("""</div> """)))})),format.raw/*51.126*/("""
            """),_display_(Seq[Any](/*52.14*/flash/*52.19*/.get("error").map/*52.36*/ { message =>_display_(Seq[Any](format.raw/*52.49*/(""" <div class="alert alert-danger"><strong>Error!</strong>"""),_display_(Seq[Any](/*52.106*/message)),format.raw/*52.113*/("""</div> """)))})),format.raw/*52.121*/("""
            """),_display_(Seq[Any](/*53.14*/content)),format.raw/*53.21*/("""
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
    <script type="text/javascript" src=""""),_display_(Seq[Any](/*71.42*/routes/*71.48*/.Assets.at("javascripts/jquery-1.9.0.min.js"))),format.raw/*71.93*/(""""></script>
    <script type="text/javascript" src=""""),_display_(Seq[Any](/*72.42*/routes/*72.48*/.Assets.at("bootstrap/js/bootstrap.min.js"))),format.raw/*72.91*/(""""></script>

  </body>
</html>
"""))}
    }
    
    def render(title:String,twitteUser:Option[TwitterUser],flash:play.api.mvc.Flash,content:Html): play.api.templates.HtmlFormat.Appendable = apply(title,twitteUser)(flash)(content)
    
    def f:((String,Option[TwitterUser]) => (play.api.mvc.Flash) => (Html) => play.api.templates.HtmlFormat.Appendable) = (title,twitteUser) => (flash) => (content) => apply(title,twitteUser)(flash)(content)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Thu Apr 03 21:52:31 JST 2014
                    SOURCE: /Users/omi-swordfish/work/play/tite_scala/app/views/main.scala.html
                    HASH: 7900350e310cb0635a429fb5541c4ce053aecf9c
                    MATRIX: 599->1|783->91|865->138|891->143|1211->427|1226->433|1280->465|1368->517|1383->523|1439->557|1527->609|1542->615|1605->656|2300->1315|2319->1325|2334->1331|2345->1346|2372->1364|2412->1366|2497->1415|2510->1419|2550->1437|2594->1445|2607->1449|2648->1467|2749->1562|2767->1571|2807->1573|2911->1656|3207->1916|3221->1921|3249->1940|3300->1953|3397->2013|3427->2020|3468->2028|3518->2042|3532->2047|3560->2066|3611->2079|3708->2139|3738->2146|3779->2154|3829->2168|3843->2173|3869->2190|3920->2203|4014->2260|4044->2267|4085->2275|4135->2289|4164->2296|4559->2655|4574->2661|4641->2706|4730->2759|4745->2765|4810->2808
                    LINES: 19->1|22->1|28->7|28->7|33->12|33->12|33->12|34->13|34->13|34->13|35->14|35->14|35->14|52->31|52->31|52->31|52->32|52->32|52->32|53->33|53->33|53->33|53->33|53->33|53->33|55->36|55->36|55->36|57->39|68->50|68->50|68->50|68->50|68->50|68->50|68->50|69->51|69->51|69->51|69->51|69->51|69->51|69->51|70->52|70->52|70->52|70->52|70->52|70->52|70->52|71->53|71->53|89->71|89->71|89->71|90->72|90->72|90->72
                    -- GENERATED --
                */
            