package connector

import java.util.concurrent.TimeUnit
import javax.inject.Singleton

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.hootsuite.circuitbreaker.CircuitBreakerBuilder
import connector.NotifyHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}
import play.api.Logger

//@Singleton
object AkkaConnector {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  def connectAkka(): Future[String] = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:9001/200"))

    responseFuture
      .onComplete {
        case Success(res) => println("akka response --> " + res)
        case Failure(_) => sys.error("something wrong")
      }

    responseFuture flatMap { res =>
      Future.successful(res.status.intValue().toString + "\n" + res.entity.toString)
    }
  }

  def tryAkka(a: Int): Future[Int] = {
    val evenNumberAsFailure: Try[Int] => Boolean = {
      case Success(n) => println("called again");n % 2 == 0
      case Failure(_) => true
    }

    val breaker =
      new CircuitBreaker(
        system.scheduler,
        maxFailures = 5,
        callTimeout = 100.seconds,
        resetTimeout = 1.minute
      ).
        onOpen(notifyMeOnOpen).
        onClose(notifyMeOnClose).
        onHalfOpen(notifyMeOnHalfOpen).
        onCallSuccess(notifyMeOnCallSuccess).
        onCallFailure(notifyMeOnCallFailure)

    breaker.withCircuitBreaker(Future(8888), evenNumberAsFailure)
  }

  val circuitBreaker = new CircuitBreakerBuilder(
    name = "MyCircuitBreaker",
    failLimit = 2,
    retryDelay = FiniteDuration(10, TimeUnit.SECONDS))
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


