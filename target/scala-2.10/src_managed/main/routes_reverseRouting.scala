// @SOURCE:/Users/omi-swordfish/work/play/tite_scala/conf/routes
// @HASH:49040974b16aa4d1a9f685200d7f948e70610089
// @DATE:Thu Apr 03 21:52:27 JST 2014

import Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._


import Router.queryString


// @LINE:13
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers {

// @LINE:10
// @LINE:9
// @LINE:8
class ReverseTwitterController {
    

// @LINE:10
def twitterLogout(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "twitterLogout")
}
                                                

// @LINE:8
def twitterLogin(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "twitterLogin")
}
                                                

// @LINE:9
def twitterOAuthCallback(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "twitterOAuthCallback")
}
                                                
    
}
                          

// @LINE:13
class ReverseAssets {
    

// @LINE:13
def at(file:String): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
}
                                                
    
}
                          

// @LINE:7
// @LINE:6
class ReverseApplication {
    

// @LINE:6
def index(p:Int = 1): Call = {
   Call("GET", _prefix + queryString(List(if(p == 1) None else Some(implicitly[QueryStringBindable[Int]].unbind("p", p)))))
}
                                                

// @LINE:7
def authTest(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "authTest")
}
                                                
    
}
                          
}
                  


// @LINE:13
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.javascript {

// @LINE:10
// @LINE:9
// @LINE:8
class ReverseTwitterController {
    

// @LINE:10
def twitterLogout : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.TwitterController.twitterLogout",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "twitterLogout"})
      }
   """
)
                        

// @LINE:8
def twitterLogin : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.TwitterController.twitterLogin",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "twitterLogin"})
      }
   """
)
                        

// @LINE:9
def twitterOAuthCallback : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.TwitterController.twitterOAuthCallback",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "twitterOAuthCallback"})
      }
   """
)
                        
    
}
              

// @LINE:13
class ReverseAssets {
    

// @LINE:13
def at : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Assets.at",
   """
      function(file) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file)})
      }
   """
)
                        
    
}
              

// @LINE:7
// @LINE:6
class ReverseApplication {
    

// @LINE:6
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Application.index",
   """
      function(p) {
      return _wA({method:"GET", url:"""" + _prefix + """" + _qS([(p == null ? null : (""" + implicitly[QueryStringBindable[Int]].javascriptUnbind + """)("p", p))])})
      }
   """
)
                        

// @LINE:7
def authTest : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Application.authTest",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "authTest"})
      }
   """
)
                        
    
}
              
}
        


// @LINE:13
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.ref {


// @LINE:10
// @LINE:9
// @LINE:8
class ReverseTwitterController {
    

// @LINE:10
def twitterLogout(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.TwitterController.twitterLogout(), HandlerDef(this, "controllers.TwitterController", "twitterLogout", Seq(), "GET", """""", _prefix + """twitterLogout""")
)
                      

// @LINE:8
def twitterLogin(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.TwitterController.twitterLogin(), HandlerDef(this, "controllers.TwitterController", "twitterLogin", Seq(), "GET", """""", _prefix + """twitterLogin""")
)
                      

// @LINE:9
def twitterOAuthCallback(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.TwitterController.twitterOAuthCallback(), HandlerDef(this, "controllers.TwitterController", "twitterOAuthCallback", Seq(), "GET", """""", _prefix + """twitterOAuthCallback""")
)
                      
    
}
                          

// @LINE:13
class ReverseAssets {
    

// @LINE:13
def at(path:String, file:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Assets.at(path, file), HandlerDef(this, "controllers.Assets", "at", Seq(classOf[String], classOf[String]), "GET", """ Map static resources from the /public folder to the /assets URL path""", _prefix + """assets/$file<.+>""")
)
                      
    
}
                          

// @LINE:7
// @LINE:6
class ReverseApplication {
    

// @LINE:6
def index(p:Int): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Application.index(p), HandlerDef(this, "controllers.Application", "index", Seq(classOf[Int]), "GET", """ Home page""", _prefix + """""")
)
                      

// @LINE:7
def authTest(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Application.authTest(), HandlerDef(this, "controllers.Application", "authTest", Seq(), "GET", """""", _prefix + """authTest""")
)
                      
    
}
                          
}
        
    