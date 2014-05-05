import sbt._
import Keys._
import play.Project._
// import com.github.play2war.plugin._

object ApplicationBuild extends Build {

  val appName         = "playframework_crud_scala"
  val appVersion      = "1.0"

  val scalacOptions   = Seq(
  	 "-unchecked"
  	,"-deprecation"
  	,"-feature"
  )

  val appDependencies = Seq(
    // Add your project dependencies here,
		 jdbc
		,anorm
		,cache
		,"mysql" % "mysql-connector-java" % "5.1.20"
		,"org.twitter4j" % "twitter4j-core" % "3.0.3"
		//,"org.twitter4j" % "twitter4j" % "3.0.5"
  )

  // val main = play.Project(appName, appVersion, appDependencies)
  //   .settings(Play2WarPlugin.play2WarSettings: _*)
  //   .settings(
  //     // Add your own project settings here                                             
  //     Play2WarKeys.servletVersion := "3.0"
  // )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
