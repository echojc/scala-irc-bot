package net.mtgto.irc

import net.mtgto.irc.event._

import com.typesafe.config.Config
import com.typesafe.config.ConfigRenderOptions
import org.pircbotx.{PircBotX, User => PircUser}
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events._
import org.slf4j.LoggerFactory

import java.io.File
import java.util.Date
import java.util.{Timer, TimerTask}

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Try

class DefaultClient[T <: PircBotX](val settings: Config) extends ListenerAdapter[T] with Client { client ⇒
  val logger = LoggerFactory.getLogger(getClass)

  val renderOptions = ConfigRenderOptions.concise.setFormatted(true)
  logger.debug(s"got config: ${settings.root.render(renderOptions)}")

  val innerClient: PircBotX = new PircBotX
  innerClient.getListenerManager.addListener(this)

  val loadedBotsLock = new Object()
  var _loadedBots: Seq[Bot] = Seq.empty
  def reloadBots(): Unit = loadedBotsLock.synchronized {
    _loadedBots foreach (_.onUnload(this))

    val botsConfig = settings.getObject("bots").toConfig
    val bots = botsConfig.root.entrySet map (_.getKey) toSeq

    logger.info(s"Found bots in config: [$bots]")

    _loadedBots = bots flatMap { bot ⇒
      val botConfig = botsConfig.getObject(bot).toConfig
      loadBot(bot.replace("-", "."), botConfig)
    }
  }
  override def bots = loadedBotsLock.synchronized {
    _loadedBots
  }

  val timer = new Timer("TimerSystem", /*isDaemon =*/ true)
  val timerTask = new TimerTask {
    override def run(): Unit = {
      client.bots foreach (_.onTimer(client))
    }
  }
  timer.schedule(timerTask, 0, settings.getLong("timer-delay"))

  def loadBot(className: String, botConfig: Config): Option[Bot] = {
    Try(getClass.getClassLoader.loadClass(className)).toOption match {
      case Some(botClazz) ⇒
        val ctors = botClazz.getConstructors
        val withConfigCtorOption = ctors find (_.getParameterTypes.toSeq == Seq(classOf[Config]))
        withConfigCtorOption match {
          case Some(ctor) ⇒
            logger.info(s"loaded bot [$className] with config ${botConfig.root.render(renderOptions)}")
            Some(ctor.newInstance(botConfig).asInstanceOf[Bot])
          case None ⇒
            logger.info(s"loaded bot [$className] with no config")
            Some(botClazz.getConstructor().newInstance().asInstanceOf[Bot])
        }
      case None ⇒
        logger.warn(s"could not find a bot with name [$className]")
        None
    }
  }

  /**
   * a map channel names to user's nicknames.
   */
  val channelUsers = collection.mutable.HashMap.empty[String, collection.mutable.Set[String]]

  override def connect = {
    reloadBots()

    innerClient.setEncoding(settings.getString("encoding"))
    innerClient.setName(settings.getString("nickname"))
    innerClient.setLogin(settings.getString("username"))
    innerClient.setVersion(settings.getString("realname"))
    innerClient.setMessageDelay(settings.getLong("message-delay"))

    val hostname = settings.getString("hostname")
    val port = settings.getInt("port")
    settings.getString("password") match {
      case "" =>
        innerClient.connect(hostname, port)
      case password @ _ =>
        innerClient.connect(hostname, port, password)
    }

    val loadedBots = bots map (_.getClass.getSimpleName)
    val channels = settings.getStringList("channels")
    channels foreach { channel ⇒
      innerClient.joinChannel(channel)
      sendMessage(channel, s"Started bots: ${loadedBots.mkString(", ")}")
    }
  }

  override def disconnect = {
    innerClient.quitServer
    bots foreach (_.onDisconnect(this))
  }

  override def isConnected: Boolean = {
    innerClient.isConnected
  }

  override def getBot(name: String): Option[Bot] = {
    bots.find(_.getClass.getCanonicalName == name)
  }

  override def getUsers(channel: String): Set[String] = {
    channelUsers.get(channel).map(_.toSet).getOrElse(Set.empty)
  }

  override def sendNotice(target: String, text: String) = {
    innerClient.sendNotice(target, text)
  }

  override def sendMessage(target: String, text: String) = {
    innerClient.sendMessage(target, text)
  }

  override def sendRawLine(line: String) = {
    innerClient.sendRawLine(line)
  }

  override def sendDccFile(nick: String, file: java.io.File, timeout: Int = 120000) = {
    innerClient.dccSendFile(file, innerClient.getUser(nick), timeout)
  }

  override def onMessage(event: MessageEvent[T]): Unit = {
    val message = Message(
      channel = event.getChannel.getName,
      nickname = event.getUser.getNick,
      username = event.getUser.getLogin,
      hostname = event.getUser.getServer,
      text = event.getMessage,
      date = new Date(event.getTimestamp))
    bots foreach (_.onMessage(this, message))
  }

  override def onPrivateMessage(event: PrivateMessageEvent[T]) = {
    val privateMessage = PrivateMessage(
      nickname = event.getUser.getNick,
      username = event.getUser.getLogin,
      hostname = event.getUser.getServer,
      text = event.getMessage,
      date = new Date(event.getTimestamp))
    bots foreach (_.onPrivateMessage(this, privateMessage))
  }

  override def onNotice(event: NoticeEvent[T]) = {
    val notice = Notice(
      target = event.getChannel.getName,
      sourceNickname = event.getUser.getNick,
      sourceUsername = event.getUser.getLogin,
      sourceHostname = event.getUser.getServer,
      text = event.getMessage,
      date = new Date(event.getTimestamp))
    bots foreach (_.onNotice(this, notice))
  }

  override def onInvite(event: InviteEvent[T]) = {
    val sender = event.getBot.getUser(event.getUser)
    val invite = Invite(
      channel = event.getChannel,
      targetNickname = event.getBot.getUserBot.getNick,
      sourceNickname = sender.getNick,
      sourceUsername = sender.getLogin,
      sourceHostname = sender.getServer,
      date = new Date(event.getTimestamp))
    bots foreach (_.onInvite(this, invite))
  }

  override def onJoin(event: JoinEvent[T]) = {
    val join = Join(
      channel = event.getChannel.getName,
      nickname = event.getUser.getNick,
      username = event.getUser.getLogin,
      hostname = event.getUser.getServer,
      date = new Date(event.getTimestamp))
    bots foreach (_.onJoin(this, join))
  }

  override def onKick(event: KickEvent[T]) = {
    val kick = Kick(
      channel = event.getChannel.getName,
      targetNickname = event.getRecipient.getNick,
      sourceNickname = event.getSource.getNick,
      sourceUsername = event.getSource.getLogin,
      sourceHostname = event.getSource.getServer,
      reason = event.getReason,
      date = new Date(event.getTimestamp))
    bots foreach (_.onKick(this, kick))
  }

  override def onMode(event: ModeEvent[T]) = {
    val mode = Mode(
      channel = event.getChannel.getName,
      nickname = event.getUser.getNick,
      username = event.getUser.getLogin,
      hostname = event.getUser.getServer,
      mode = event.getMode,
      date = new Date(event.getTimestamp))
    bots foreach (_.onMode(this, mode))
  }

  override def onTopic(event: TopicEvent[T]) = {
    val topic = Topic(
      channel = event.getChannel.getName,
      nickname = event.getUser.getNick,
      topic = event.getTopic,
      date = new Date(event.getTimestamp))
    bots foreach (_.onTopic(this, topic))
  }

  override def onNickChange(event: NickChangeEvent[T]) = {
    val nickChange = NickChange(
      oldNickname = event.getOldNick,
      newNickname = event.getNewNick,
      username = event.getUser.getLogin,
      hostname = event.getUser.getServer,
      date = new Date(event.getTimestamp))
    bots foreach (_.onNickChange(this, nickChange))
  }

  override def onOp(event: OpEvent[T]) = {
    val op = Op(
      channel = event.getChannel.getName,
      targetNickname = event.getRecipient.getNick,
      sourceNickname = event.getSource.getNick,
      sourceUsername = event.getSource.getLogin,
      sourceHostname = event.getSource.getServer,
      date = new Date(event.getTimestamp))
    bots foreach (_.onOp(this, op))
  }

  override def onPart(event: PartEvent[T]) = {
    val part = Part(
      channel = event.getChannel.getName,
      nickname = event.getUser.getNick,
      username = event.getUser.getLogin,
      hostname = event.getUser.getServer,
      date = new Date(event.getTimestamp))
    bots foreach (_.onPart(this, part))
  }

  override def onQuit(event: QuitEvent[T]) = {
    val quit = Quit(
      nickname = event.getUser.getNick,
      username = event.getUser.getLogin,
      hostname = event.getUser.getServer,
      reason = event.getReason,
      date = new Date(event.getTimestamp))
    bots foreach (_.onQuit(this, quit))
  }
}

