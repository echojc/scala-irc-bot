name := "echobot"

organization := "sh.echo"

version := "1.0.0"

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.pircbotx" % "pircbotx" % "1.8",
  "ch.qos.logback" % "logback-classic" % "1.0.7"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding",
  "UTF8",
  "-feature",
  "-language:postfixOps"
)

unmanagedBase in Runtime <<= baseDirectory(_ / "bots")

initialCommands := """
  import net.mtgto.irc._
  import akka.actor._
  import scala.concurrent._
  import duration._
  import ExecutionContext.Implicits.global
"""
