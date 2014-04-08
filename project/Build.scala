import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "hgi-web_usermod"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    cache,
    "com.typesafe.play" %% "play-slick" % "0.6.0.1",
    "org.scalaz" %% "scalaz-core" % "7.0.0",
    "mysql" % "mysql-connector-java" % "5.1.25",
    "commons-codec" % "commons-codec" % "1.8"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
