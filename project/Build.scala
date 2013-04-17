import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "kwatchdog-app"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.mongodb" %% "casbah" % "2.5.0",
    jdbc,
    anorm
  )
  
  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"   
  )

}
