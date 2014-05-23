package sh.echo.echobot

import net.mtgto.irc._
import net.mtgto.irc.event._

class EchoBot extends Bot {

  override def onMessage(client: Client, message: Message) = {
    client.logger.info(s"got message [$message]")
    client.sendMessage(message.channel, s"""${message.nickname} said: "${message.text}"""")
  }
}
