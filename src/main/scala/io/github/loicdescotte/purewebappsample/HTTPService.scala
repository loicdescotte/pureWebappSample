package io.github.loicdescotte.purewebappsample

import cats.implicits._
import scalaz.zio.console.putStrLn
import scalaz.zio.{App, IO, Task, ZIO}
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import doobie.util.transactor.Transactor
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
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
case class HTTPService(databaseAccess: StockDAO) extends Http4sDsl[Task] {

  val logger = getLogger(classOf[HTTPService])

  val routes: HttpRoutes[Task] = HttpRoutes.of[Task] {

    case GET -> Root / "stock" / IntVar(stockId) =>
      // retrieve stock in database
      val stockDbResult: IO[StockError, Stock] = databaseAccess.currentStock(stockId).flatMap(stock => //work on the value inside the IO
        IO.fromEither(Stock.validate(stock))
      )
      stockOrErrorResponse(stockDbResult)

    case PUT -> Root / "stock" / IntVar(stockId) / IntVar(updateValue) =>
      stockOrErrorResponse(databaseAccess.updateStock(stockId, updateValue))
  }

  def stockOrErrorResponse(stockResponse: IO[StockError, Stock]): Task[Response[Task]] = {
    stockResponse.foldM(
      stockError => {
        IO(logger.error(stockError.getMessage, stockError))
        Conflict(Json.obj("Error" -> Json.fromString(stockError.getMessage)))
      },
      stock => Ok(stock.asJson))
  }

}

//Zio App will execute IO unsafe calls (i.e. all the side effects) and manage threading
object Server extends App {

  val xa = Transactor.fromDriverManager[Task](
    "org.h2.Driver",
    "jdbc:h2:mem:poc;INIT=RUNSCRIPT FROM 'src/main/resources/sql/create.sql'"
    , "sa", ""
  )

  def run(args: List[String]) = ZIO.runtime[Environment].flatMap { implicit rts =>
    //Start the server
    val databaseAccess = new StockDAO(xa)
    BlazeServerBuilder[Task]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(HTTPService(databaseAccess).routes.orNotFound)
      .serve
      .compile.drain.as(0)
  }.catchAll(e => putStrLn(s"Server failed with '$e'") *> ZIO.succeed(1))

}
