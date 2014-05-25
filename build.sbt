scalaVersion := "2.11.1"

version := "1.0.0"

resolvers ++= Seq(
  "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.pircbotx" % "pircbotx" % "1.8",
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "com.typesafe" % "config" % "1.2.1"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding",
  "UTF8",
  "-feature",
  "-language:postfixOps"
)

initialCommands := """
  import net.mtgto.irc._
  import akka.actor._
  import scala.concurrent._
  import duration._
  import ExecutionContext.Implicits.global
"""
