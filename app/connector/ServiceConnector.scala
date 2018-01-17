package connector

import javax.inject.{Inject, Named, Singleton}

import play.api.libs.json.JsValue
import play.api.libs.ws._
import play.api.mvc._
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.pattern.CircuitBreaker
import akka.pattern.pipe
import akka.actor.{ActorLogging, ActorRef, ActorSystem}
import connector.NotifyHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

@Singleton
class ServiceConnector @Inject()(ws: WSClient) extends Controller {

  val system = akka.actor.ActorSystem("my-system")

  val exceptionAsFailure: Try[WSResponse] ⇒ Boolean = {
    case Success(x) if x.status == OK => Logger.warn("\ntrue exception block ---- true"); false
    case _ => Logger.warn("\nfalse exception block ---- false"); true
  }


  def connect() = {

    //val url = "200"
    //val url = "50X"
    val url = "40X"

    val breaker =
      new CircuitBreaker(
        system.scheduler,
        maxFailures = 1,
        callTimeout = 5.seconds,
        resetTimeout = 10.seconds).
        onOpen(notifyMeOnOpen).
        onClose(notifyMeOnClose).
        onHalfOpen(notifyMeOnHalfOpen).
        onCallSuccess(notifyMeOnCallSuccess).
        onCallFailure(notifyMeOnCallFailure)

    val request: Future[WSResponse] = breaker.withCircuitBreaker(
      ws.url(s"http://localhost:9001/$url").get()
    )

    Logger.warn("\n\n****circuit status " + breaker.isClosed)
    request flatMap {
      response =>
        /*response.status match {
          case 200 =>
            Logger.info("call successful")
            Future.successful(response.json.as[String])
          case _ =>
            /*Logger.warn("\n\n Failing circuit " + breaker.isOpen)
            Future.failed(new Exception(response.json.as[String]))*/
            Future.failed(response)
        }*/
        Future.successful(response.json.as[String])
    }
  }

}