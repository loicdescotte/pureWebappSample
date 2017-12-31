package io.github.loicdescotte.purewebappsample

import cats.data.EitherT
import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.StreamApp
import io.circe._
import io.github.loicdescotte.purewebappsample.dao.StockDAO
import io.github.loicdescotte.purewebappsample.model.{Stock, StockError}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

/**
  * HTTP routes definition
  */
object HTTPService extends Http4sDsl[IO] {

  def service(databaseAccess: StockDAO) = HttpService[IO] {

    case GET -> Root / "stock" =>

      val stockResult = databaseAccess.currentStock.map(stockOrError => //work on the value inside the IO
        //flatMap either values (stock or 2 possible error types: NonReachableStock and EmptyStock)
        stockOrError.flatMap(Stock.validate)
      )

      stockResult.flatMap {
        case Right(stock) => Ok(Json.obj("stock" -> Json.fromInt(stock.value)))
        case Left(stockError: StockError) => Conflict(Json.obj("stock" -> Json.fromString(stockError.getMessage)))
      }
  }

}

//StreamApp will execute IO unsafe calls (i.e. all the side effects) and manage threading
object Server extends StreamApp[IO] {
  override def stream(args: List[String], requestShutdown: IO[Unit]) = {

    val xa = Transactor.fromDriverManager[IO](
      "org.h2.Driver",
      "jdbc:h2:mem:poc;INIT=RUNSCRIPT FROM 'src/main/resources/sql/create.sql'"
      , "sa", ""
    )

    //Start the server
    val databaseAccess = new StockDAO(xa)
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(HTTPService.service(databaseAccess), "/")
      .serve
  }
}
