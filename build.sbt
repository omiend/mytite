name := "tite_scala"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.twitter4j" % "twitter4j" % "3.0.5",
  "mysql" % "mysql-connector-java" % "5.1.20",
  "org.twitter4j" % "twitter4j-core" % "3.0.3"
)     

play.Project.playScalaSettings
