package io.github.loicdescotte.purewebappsample

import io.github.loicdescotte.purewebappsample.ExtServices.{ExtServices, StockDAO}
import io.github.loicdescotte.purewebappsample.model.{Stock, StockDBAccessError, StockError, StockNotFound}
import org.http4s._
import org.http4s.syntax.literals._
import org.http4s.syntax.kleisli._
import zio.clock.Clock
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._
import zio.{IO, ZLayer}


class StockSpec extends DefaultRunnableSpec {

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

  def spec = suite("Stock HTTP Service") {

    testM("return 200 and current stock") {
      val request = Request[STask](Method.GET, uri"""/stock/1""")
      val response: SResponse = HTTPService.routes.orNotFound.run(request)
      response.flatMap(r => assertM(r.as[String])(equalTo("""{"id":1,"value":10}""")))
        .provideLayer(externalServicesTest)
    }

    testM("return 200 and updated stock"){
      val request = Request[STask](Method.PUT, uri"""/stock/1/5""")

      val asserts = for {
        response <- HTTPService.routes.orNotFound.run(request)
        a1 = assert(response.status)(equalTo(Status.Ok))
        a2 <- assertM(response.as[String])(equalTo("""{"id":1,"value":15}"""))
      } yield a1 && a2

      asserts.provideLayer(externalServicesTest)
    }

    testM("return empty stock error"){
      val request = Request[STask](Method.GET, uri"""/stock/3""")
      val asserts = for {
        response <- HTTPService.routes.orNotFound.run(request)
        a1 = assert(response.status)(equalTo(Status.Conflict))
        a2 <- assertM(response.as[String])(equalTo("""{"Error":"Stock is empty"}"""))
      } yield a1 && a2

      asserts.provideLayer(externalServicesTest)
    }



  }

  //
  //  "Stock HTTP Service" should {
  //    //

  //    "return stock not found error" in {
  //      val request = Request[STask](Method.GET, uri"""/stock/4""")
  //      val stockResponse = testRuntime.unsafeRun(HTTPService.routes.orNotFound.run(request))
  //      stockResponse.status must beEqualTo(Status.NotFound)
  //      testRuntime.unsafeRun(stockResponse.as[String]) must beEqualTo("""{"Error":"Stock not found"}""")
  //    }
  //
  //    "return database error" in {
  //      val request = Request[STask](Method.GET, uri"""/stock/99""")
  //      val stockResponse = testRuntime.unsafeRun(HTTPService.routes.orNotFound.run(request))
  //      stockResponse.status must beEqualTo(Status.InternalServerError)
  //      testRuntime.unsafeRun(stockResponse.as[String]) must contain("""BOOM""")
  //    }
  //  }
}
