package io.github.loicdescotte.purewebappsample

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.github.loicdescotte.purewebappsample.model.{EmptyStock, Stock, StockError, StockNotFound}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import org.slf4j.LoggerFactory._
import zio.interop.catz._
import zio.{IO, TaskR, ZIO}

/**
  * HTTP routes definition
  */
object HTTPService extends Http4sDsl[STask] {

  val logger = getLogger(this.getClass)

  //dependency injection
  val stockDao = ZIO.access[ExtServices](_.stockDao)

  val routes: HttpRoutes[STask] = HttpRoutes.of[STask] {

    case GET -> Root / "stock" / IntVar(stockId) =>
      // retrieve stock in database
      val stockDbResult: ZIO[ExtServices, StockError, Stock] = for {
        dao <- stockDao
        stock <- dao.currentStock(stockId)
        result <- IO.fromEither(Stock.validate(stock))
      } yield result

      stockOrErrorResponse(stockDbResult)

    case PUT -> Root / "stock" / IntVar(stockId) / IntVar(updateValue) =>
      stockOrErrorResponse(stockDao.flatMap(_.updateStock(stockId, updateValue)))
  }

  def stockOrErrorResponse(stockResponse: ZIO[ExtServices, StockError, Stock]): TaskR[ExtServices, Response[STask]] = {
    stockResponse.foldM({
      //error cases
      case EmptyStock =>  Conflict(Json.obj("Error" -> Json.fromString("Stock is empty")))

      case StockNotFound => NotFound(Json.obj("Error" -> Json.fromString("Stock not found")))

      case stockError =>
        IO(logger.error(stockError.getMessage))
          .flatMap(_ => InternalServerError(Json.obj("BOOM" -> Json.fromString(stockError.getMessage))))
    },
      //success case
      stock => Ok(stock.asJson))
  }

}

object Server extends CatsApp {

  //Runtime will execute IO unsafe calls (i.e. all the side effects) and manage threading
  val program = ZIO.runtime[ExtServices].flatMap { implicit runtime =>
    //Start the server
    BlazeServerBuilder[STask]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(HTTPService.routes.orNotFound)
      .serve
      .compile.drain
  }

  //plug the real service
  override def run(args: List[String]) = program.provide(ExtServicesLive).fold(_ => 1, _ => 0)
}


