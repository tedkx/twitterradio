import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "twitterradio"
  	val appVersion      = "1.0-SNAPSHOT"

val appDependencies = Seq(
  	"postgresql" % "postgresql" % "9.1-901.jdbc4",
    "securesocial" %% "securesocial" % "master-SNAPSHOT",
    "org.twitter4j" % "twitter4j-core" % "3.0.3",
    javaCore,
    javaJdbc,
    javaEbean,
    "mysql" % "mysql-connector-java" % "5.1.21", jdbc, anorm
  )

    val main = play.Project(appName, appVersion, appDependencies)
    .settings(
      resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
    )

}