package sh.echo

import com.typesafe.config.ConfigFactory
import net.mtgto.irc._
import net.mtgto.irc.event._
import org.slf4j.LoggerFactory

object Main extends App {
  val rootConfig = ConfigFactory.load()
  val clientConfig = rootConfig.getObject("scala-irc-bot").toConfig
  val client = new DefaultClient(clientConfig)
  client.connect()

  Thread.sleep(20000)
  client.disconnect()
}

class TestBot extends Bot {
  val logger = LoggerFactory.getLogger(getClass)

  override def onMessage(client: Client, message: Message) = {
    logger.info(message.toString)
  }

  override def onJoin(client: Client, join: Join) = {
    logger.info(join.toString)
    //client.sendMessage(join.channel, "hi")
  }

  override def onPrivateMessage(client: Client, message: PrivateMessage) = { logger.info(message.toString) }

  override def onNotice(client: Client, notice: Notice) = { logger.info(notice.toString) }

  override def onInvite(client: Client, invite: Invite) = { logger.info(invite.toString) }

  override def onKick(client: Client, kick: Kick) = { logger.info(kick.toString) }

  override def onMode(client: Client, mode: Mode) = { logger.info(mode.toString) }

  override def onTopic(client: Client, topic: Topic) = { logger.info(topic.toString) }

  override def onNickChange(client: Client, nickChange: NickChange) = { logger.info(nickChange.toString) }

  override def onOp(client: Client, op: Op) = { logger.info(op.toString) }

  override def onPart(client: Client, part: Part) = { logger.info(part.toString) }

  override def onQuit(client: Client, quit: Quit) = { logger.info(quit.toString) }

  override def onTimer(client: Client) = { logger.info("timer!") }

  override def onUnload(client: Client) = { logger.info("unload!") }

  override def onDisconnect(client: Client) = { logger.info("disconnect!") }
}
