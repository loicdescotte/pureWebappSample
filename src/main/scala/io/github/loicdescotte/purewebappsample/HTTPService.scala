package io.github.loicdescotte.purewebappsample

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import io.circe._, io.circe.generic.auto._, io.circe.syntax._
import io.github.loicdescotte.purewebappsample.dao.StockDAO
import io.github.loicdescotte.purewebappsample.model.{Stock, StockError}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.syntax.kleisli._
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.LoggerFactory._

/**
  * HTTP routes definition
  */
case class HTTPService(databaseAccess: StockDAO) extends Http4sDsl[IO] {

  val logger = getLogger(classOf[HTTPService])

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "stock" / IntVar(stockId) =>

      // retrieve stock in database
      val stockDbResult = databaseAccess.currentStock(stockId).map(stockOrError => //work on the value inside the IO
        //flatMap either values (stock or 2 possible error types: NonReachableStock and EmptyStock)
        stockOrError.flatMap(Stock.validate)
      )

      // transform to HTTP response
      stockDbResult.flatMap(s => stockOrErrorResponse(s))

    case PUT -> Root / "stock" / IntVar(stockId) / IntVar(updateValue) =>

      val stockDbResult: IO[Either[StockError, Stock]] = databaseAccess.updateStock(stockId, updateValue)

      stockDbResult.flatMap(s => stockOrErrorResponse(s))
  }

  private def stockOrErrorResponse[A: Encoder](entity: Either[StockError, A]): IO[Response[IO]] = {
    entity match {
      case Right(e) => Ok(e.asJson)
      case Left(stockError: StockError) =>
        logger.error(stockError.getMessage, stockError)
        Conflict(Json.obj("Error" -> Json.fromString(stockError.getMessage)))
    }
  }
}

//IOApp will execute IO unsafe calls (i.e. all the side effects) and manage threading
object Server extends IOApp {

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:poc;INIT=RUNSCRIPT FROM 'src/main/resources/sql/create.sql'"
    , "sa", ""
  )

  override def run(args: List[String]): IO[ExitCode] = {
    //Start the server
    val databaseAccess = new StockDAO(xa)
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(HTTPService(databaseAccess).routes.orNotFound)
      .serve
      .compile.drain.as(ExitCode.Success)
  }
}
