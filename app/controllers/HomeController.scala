package controllers

import javax.inject._

import akka.stream.Materializer
import connector.{AkkaConnector, ServiceConnector}
import play.api.libs.ws.ahc._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit mat: Materializer) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action.async { implicit request: Request[AnyContent] =>
    val connector = new ServiceConnector(AhcWSClient())
    connector.connect.map { res =>
      Ok(s"result by calling the service: $res")
    }
  }

  def indexAkka() = Action.async { implicit request: Request[AnyContent] =>
    val connector = AkkaConnector
    connector.connectAkka.map { res =>
      Ok(s"result by calling the service: $res")
    }
  }

  def tryAkka(id: Int) = Action.async { implicit request: Request[AnyContent] =>
    val connector = AkkaConnector
    connector.tryAkka(id).map { res =>
      Ok(s"result by calling the service: $res")
    }
  }

  def tryHoot(id: String) = Action.async { implicit request: Request[AnyContent] =>
    val connector = AkkaConnector
    connector.tryHootSuite(id).map { res =>
      Ok(s"result by calling the service: $res")
    }
  }
}
