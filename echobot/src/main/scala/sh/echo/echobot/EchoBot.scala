package sh.echo.echobot

import scala.util.Failure
import scala.util.Success

import net.mtgto.irc._
import net.mtgto.irc.event._
import org.slf4j.LoggerFactory

class EchoBot extends Bot {
  val logger = LoggerFactory.getLogger(getClass)

  val musicService = new MusicService()
  import musicService.system.dispatcher

  def fail(client: Client, channel: String, e: Throwable): Unit = {
    logger.error(s"failed request: [$e]")
    client.sendMessage(channel, s"Request failed: [${e.getMessage}]")
  }

  override def onMessage(client: Client, message: Message) = {
    logger.debug(s"got message [$message]")

    val Message(channel, _, _, _, text, _) = message
    client.sendMessage(channel, s"""sending GET request for "$text"""")

    musicService.vanillaGet(text) andThen {
      case Success(response) ⇒
        logger.debug(s"vanillaGet request for [$text] succeeded")
        client.sendMessage(channel, response)
      case Failure(e) ⇒
        fail(client, channel, e)
    }
  }

  override def onDisconnect(client: Client) = {
    musicService.close()
  }
}
