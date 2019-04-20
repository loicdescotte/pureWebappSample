package io.github.loicdescotte.purewebappsample

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import io.circe._
import io.github.loicdescotte.purewebappsample.dao.StockDAO
import io.github.loicdescotte.purewebappsample.model.{Stock, StockError}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.syntax.kleisli._
import org.http4s.server.blaze.BlazeServerBuilder

/**
  * HTTP routes definition
  */
case class HTTPService(databaseAccess: StockDAO) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "stock" / IntVar(stockId) =>

      val stockResult = databaseAccess.currentStock(stockId).map(stockOrError => //work on the value inside the IO
        //flatMap either values (stock or 2 possible error types: NonReachableStock and EmptyStock)
        stockOrError.flatMap(Stock.validate)
      )

      stockResult.flatMap {
        case Right(stock) => Ok(Json.obj("stock" -> Json.fromInt(stock.value)))
        case Left(stockError: StockError) => Conflict(Json.obj("stock" -> Json.fromString(stockError.getMessage)))
      }

    case PUT -> Root / "stock" / IntVar(stockId) / IntVar(updateValue) =>

      val stockResult: IO[Either[StockError, Stock]] = databaseAccess.updateStock(stockId, updateValue) *> databaseAccess.currentStock(stockId)

      stockResult.flatMap {
        case Right(stock) => Ok(Json.obj("stock" -> Json.fromInt(stock.value)))
        case Left(stockError: StockError) => Conflict(Json.obj("stock" -> Json.fromString(stockError.getMessage)))
      }

  }

}

//IOApp will execute IO unsafe calls (i.e. all the side effects) and manage threading
object Server extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val xa = Transactor.fromDriverManager[IO](
      "org.h2.Driver",
      "jdbc:h2:mem:poc;INIT=RUNSCRIPT FROM 'src/main/resources/sql/create.sql'"
      , "sa", ""
    )

    //Start the server
    val databaseAccess = new StockDAO(xa)
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(HTTPService(databaseAccess).routes.orNotFound)
      .serve
      .compile.drain.as(ExitCode.Success)
  }
}
