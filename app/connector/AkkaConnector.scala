package connector

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.hootsuite.circuitbreaker
import com.hootsuite.circuitbreaker.CircuitBreakerBuilder
import connector.NotifyHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import play.api.Logger

object AkkaConnector {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val circuitBreaker: circuitbreaker.CircuitBreaker = new CircuitBreakerBuilder(
    name = "MyCircuitBreaker",
    failLimit = 2,
    retryDelay = FiniteDuration(20, TimeUnit.SECONDS))
    .withStateChangeListeners(List(defaultLoggingStateChangeListener))
    .withInvocationListeners(List(defaultLoggingInvocationListener)).build()

  def tryHootSuite(status: String): Future[Unit] = {
    circuitBreaker.async() {
      val request: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://localhost:9001/$status"))
      request map { res =>
        res.status.intValue match {
          case 200 =>  Logger.info(s"Request response: ${res.entity.toString}")
          case _ => Future.failed(throw new RuntimeException("error happens"))
        }
      }
    }
  }

}

