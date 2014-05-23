package sh.echo.echobot

import net.mtgto.irc._

object Main extends App {
  val client: Client = new DefaultClient(EchoConfig)
  client.connect

  Thread sleep 10000

  client.disconnect
}

