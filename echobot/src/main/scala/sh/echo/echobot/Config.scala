package sh.echo.echobot

import net.mtgto.irc.Config
import net.mtgto.irc.config.BotConfig

object Config extends Config {
  val hostname = "irc.movio.co"
  val port = 6667
  val password = None
  val encoding = "utf-8"
  val messageDelay = 2000
  val timerDelay = 60000
  val nickname = "echobot"
  val username = "echobot"
  val realname = "echobot"

  val channels = Array("#test")

  val bots = Array[(String, Option[BotConfig])](
    ("sh.echo.echobot.EchoBot", None)
  )
}
