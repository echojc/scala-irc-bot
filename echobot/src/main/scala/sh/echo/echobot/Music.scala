package sh.echo.echobot

import scala.concurrent.Future
import akka.actor.ActorSystem

import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._

class MusicService {

  implicit val system = ActorSystem()
  import system.dispatcher

  val pipeline: HttpRequest ⇒ Future[HttpResponse] = sendReceive

  def vanillaGet(query: String): Future[String] = {
    pipeline(Get(query)) map (_.toString)
  }

  def close(): Unit = {
    system.shutdown()
  }
}
