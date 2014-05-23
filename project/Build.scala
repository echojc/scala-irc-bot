import sbt._
import sbt.Def.Setting
import Keys._

object MyBuild extends Build {

  lazy val root = (
    project
    in file(".")
    aggregate(`scala-irc-bot`, echobot)
  )

  lazy val `scala-irc-bot` = project settings (standardSettings: _*)
  lazy val echobot = project dependsOn `scala-irc-bot` settings (standardSettings: _*)

  lazy val standardSettings = Seq[Setting[_]](
    scalaVersion := "2.11.1",
    resolvers ++= Seq(
      "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(
      "org.pircbotx" % "pircbotx" % "1.8",
      "ch.qos.logback" % "logback-classic" % "1.0.7"
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF8",
      "-feature",
      "-language:postfixOps"
    ),
    initialCommands := """
      import net.mtgto.irc._
      import akka.actor._
      import scala.concurrent._
      import duration._
      import ExecutionContext.Implicits.global
    """
  )
}
