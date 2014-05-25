package sh.echo

import com.typesafe.config.Config
import net.mtgto.irc._
import net.mtgto.irc.event._
import org.slf4j.LoggerFactory

class SystemBot(config: Config) extends Bot {
  val logger = LoggerFactory.getLogger(getClass)

  val isShutdownEnabled = config.getBoolean("shutdown-enabled")
  val commandPrefix = config.getString("command-prefix")

  override def onMessage(client: Client, message: Message) = {
    val Message(channel, _, _, _, text, _) = message
    if (text startsWith (commandPrefix)) {
      logger.debug(s"got command: [$text]")

      val command = text.split(' ').head.drop(commandPrefix.length)
      command match {
        case "reload" ⇒
          client match {
            case client: DefaultClient[_] ⇒
              logger.info("reloading bots...")
              client.reloadBots()
              val loadedBots = client.bots map (_.getClass.getSimpleName)
              client.sendMessage(channel, s"reloaded bots: ${loadedBots.mkString(", ")}")
              logger.info("successfully reloaded bots")
            case _ ⇒
              logger.warn(s"Using a client that cannot reload bots!")
              client.sendMessage(channel, "This client cannot reload bots!")
          }
        case "shutdown" if isShutdownEnabled ⇒
          logger.info("shutting down!")
          client.sendMessage(channel, "I am shutting down!")
          client.disconnect
          System.exit(0)
        case "shutdown" if !isShutdownEnabled ⇒
          logger.info("shutdown triggered but is disabled")
          client.sendMessage(channel, "Shutdown is disabled!")
        case _ ⇒
          logger.debug(s"got unknown command [$command]")
      }
    }
  }
}
