// @SOURCE:/Users/omi-swordfish/work/play/tite_scala/conf/routes
// @HASH:49040974b16aa4d1a9f685200d7f948e70610089
// @DATE:Thu Apr 03 22:11:05 JST 2014


import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._


import Router.queryString

object Routes extends Router.Routes {

private var _prefix = "/"

def setPrefix(prefix: String) {
  _prefix = prefix
  List[(String,Routes)]().foreach {
    case (p, router) => router.setPrefix(prefix + (if(prefix.endsWith("/")) "" else "/") + p)
  }
}

def prefix = _prefix

lazy val defaultPrefix = { if(Routes.prefix.endsWith("/")) "" else "/" }


// @LINE:6
private[this] lazy val controllers_Application_index0 = Route("GET", PathPattern(List(StaticPart(Routes.prefix))))
        

// @LINE:7
private[this] lazy val controllers_Application_authTest1 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("authTest"))))
        

// @LINE:8
private[this] lazy val controllers_TwitterController_twitterLogin2 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("twitterLogin"))))
        

// @LINE:9
private[this] lazy val controllers_TwitterController_twitterOAuthCallback3 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("twitterOAuthCallback"))))
        

// @LINE:10
private[this] lazy val controllers_TwitterController_twitterLogout4 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("twitterLogout"))))
        

// @LINE:13
private[this] lazy val controllers_Assets_at5 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("assets/"),DynamicPart("file", """.+""",false))))
        
def documentation = List(("""GET""", prefix,"""controllers.Application.index(p:Int ?= 1)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """authTest""","""controllers.Application.authTest"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """twitterLogin""","""controllers.TwitterController.twitterLogin"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """twitterOAuthCallback""","""controllers.TwitterController.twitterOAuthCallback"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """twitterLogout""","""controllers.TwitterController.twitterLogout"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""","""controllers.Assets.at(path:String = "/public", file:String)""")).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
  case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
  case l => s ++ l.asInstanceOf[List[(String,String,String)]] 
}}
      

def routes:PartialFunction[RequestHeader,Handler] = {

// @LINE:6
case controllers_Application_index0(params) => {
   call(params.fromQuery[Int]("p", Some(1))) { (p) =>
        invokeHandler(controllers.Application.index(p), HandlerDef(this, "controllers.Application", "index", Seq(classOf[Int]),"GET", """ Home page""", Routes.prefix + """"""))
   }
}
        

// @LINE:7
case controllers_Application_authTest1(params) => {
   call { 
        invokeHandler(controllers.Application.authTest, HandlerDef(this, "controllers.Application", "authTest", Nil,"GET", """""", Routes.prefix + """authTest"""))
   }
}
        

// @LINE:8
case controllers_TwitterController_twitterLogin2(params) => {
   call { 
        invokeHandler(controllers.TwitterController.twitterLogin, HandlerDef(this, "controllers.TwitterController", "twitterLogin", Nil,"GET", """""", Routes.prefix + """twitterLogin"""))
   }
}
        

// @LINE:9
case controllers_TwitterController_twitterOAuthCallback3(params) => {
   call { 
        invokeHandler(controllers.TwitterController.twitterOAuthCallback, HandlerDef(this, "controllers.TwitterController", "twitterOAuthCallback", Nil,"GET", """""", Routes.prefix + """twitterOAuthCallback"""))
   }
}
        

// @LINE:10
case controllers_TwitterController_twitterLogout4(params) => {
   call { 
        invokeHandler(controllers.TwitterController.twitterLogout, HandlerDef(this, "controllers.TwitterController", "twitterLogout", Nil,"GET", """""", Routes.prefix + """twitterLogout"""))
   }
}
        

// @LINE:13
case controllers_Assets_at5(params) => {
   call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        invokeHandler(controllers.Assets.at(path, file), HandlerDef(this, "controllers.Assets", "at", Seq(classOf[String], classOf[String]),"GET", """ Map static resources from the /public folder to the /assets URL path""", Routes.prefix + """assets/$file<.+>"""))
   }
}
        
}

}
     