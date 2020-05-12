package io.github.loicdescotte.purewebappsample

import io.github.loicdescotte.purewebappsample.Dependencies.StockDAO
import io.github.loicdescotte.purewebappsample.model.{Stock, StockDBAccessError, StockError, StockNotFound}
import org.http4s._
import org.http4s.syntax.literals._
import org.http4s.syntax.kleisli._
import zio.clock.Clock
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._
import zio.{IO, ZLayer}


object StockSpec extends DefaultRunnableSpec {

  val stockDAOTest: ZLayer.NoDeps[Nothing, StockDAO] = ZLayer.succeed {
    new StockDAO.Service {
      //you could also use a mocking framework here
      override def currentStock(stockId: Int): IO[StockError, Stock] = {
        stockId match {
          case 1 => IO.succeed(Stock(1, 10))
          case 2 => IO.succeed(Stock(2, 15))
          case 3 => IO.succeed(Stock(3, 0))
          case 99 => IO.fromEither(Left(StockDBAccessError(new Exception("BOOM!"))))
          case _ => IO.fromEither(Left(StockNotFound))
        }
      }

      override def updateStock(stockId: Int, updateValue: Int): IO[StockError, Stock] = {
        currentStock(stockId).map(stock => stock.copy(value = stock.value + updateValue))
      }
    }

  }

  val externalServicesTest = stockDAOTest ++ Clock.live

  val scenarios = List(
    testM("return 200 and current stock") {
      val request = Request[STask](Method.GET, uri"""/stock/1""")
      val response: SResponse = HTTPService.routes.orNotFound.run(request)
      response.flatMap(r => assertM(r.as[String])(equalTo("""{"id":1,"value":10}""")))
    },

    testM("return 200 and updated stock") {
      val request = Request[STask](Method.PUT, uri"""/stock/1/5""")

      for {
        response <- HTTPService.routes.orNotFound.run(request)
        a1 = assert(response.status)(equalTo(Status.Ok))
        a2 <- assertM(response.as[String])(equalTo("""{"id":1,"value":15}"""))
      } yield a1 && a2

    },

    testM("return empty stock error") {
      val request = Request[STask](Method.GET, uri"""/stock/3""")

      for {
        response <- HTTPService.routes.orNotFound.run(request)
        a1 = assert(response.status)(equalTo(Status.Conflict))
        a2 <- assertM(response.as[String])(equalTo("""{"Error":"Stock is empty"}"""))
      } yield a1 && a2
    },

    testM("return stock not found error") {
      val request = Request[STask](Method.GET, uri"""/stock/4""")

      for {
        response <- HTTPService.routes.orNotFound.run(request)
        a1 = assert(response.status)(equalTo(Status.NotFound))
        a2 <- assertM(response.as[String])(equalTo("""{"Error":"Stock not found"}"""))
      } yield a1 && a2
    },

    testM("return database error") {
      val request = Request[STask](Method.GET, uri"""/stock/99""")

      for {
        response <- HTTPService.routes.orNotFound.run(request)
        a1 = assert(response.status)(equalTo(Status.InternalServerError))
        a2 <- assertM(response.as[String])(containsString("""BOOM"""))
      } yield a1 && a2
    })

  override def spec = suite("Stock HTTP Service")(scenarios: _*)
    .provideLayer(externalServicesTest)
}
