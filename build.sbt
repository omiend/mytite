name := """playframeworkscala_mytite"""

version := "1.2"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
   jdbc
  ,anorm
  ,cache
  ,"mysql" % "mysql-connector-java" % "5.1.20"
  ,"org.twitter4j" % "twitter4j-core" % "4.0.2"
)
