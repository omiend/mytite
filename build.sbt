name := """playframeworkscala_mytite"""

version := "1.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
   cache
  ,jdbc
  ,anorm
  ,"org.twitter4j" % "twitter4j-core" % "4.0.2"
  ,"mysql" % "mysql-connector-java" % "5.1.20"
  ,"com.typesafe.slick" % "slick_2.11" % "2.1.0"
  ,"com.typesafe.play" % "play-slick_2.11" % "0.8.0"
  // ,"org.slf4j" % "slf4j-nop" % "1.7.7"
  ,"joda-time" % "joda-time" % "2.4"
  ,"org.joda" % "joda-convert" % "1.6"
  ,"com.github.tototoshi" %% "slick-joda-mapper" % "1.2.0"
)
