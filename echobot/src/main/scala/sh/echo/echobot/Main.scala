package sh.echo.echobot

import com.typesafe.config.ConfigFactory
import net.mtgto.irc._

object Main extends App {
  val rootConfig = ConfigFactory.load()
  val clientConfig = rootConfig.getObject("scala-irc-bot").toConfig

  val client: Client = new DefaultClient(clientConfig)

  client.connect
  Thread sleep 10000
  client.disconnect
}

