package controllers

import javax.inject._

import akka.stream.Materializer
import connector.AkkaConnector
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit mat: Materializer) extends AbstractController(cc) {

  def tryHoot(id: String) = Action.async { implicit request: Request[AnyContent] =>
    val connector = AkkaConnector
    connector.tryHootSuite(id).map { _ =>
      Ok("""<br><br><h1 align="center">Request successful</h1>""").as("text/html")
    }.recover {
      case _: Exception =>
        println("Exception occurred")
        Ok("""<br><br><h1 align="center">Exception occured</h1>""").as("text/html")
    }
  }
}
