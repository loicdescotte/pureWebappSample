package io.github.loicdescotte.purewebappsample

import io.github.loicdescotte.purewebappsample.dao.{StockDAO, StockDAOLive}
import io.github.loicdescotte.purewebappsample.model.{Stock, StockDBAccessError}
import org.http4s._
import org.http4s.syntax.kleisli._
import org.scalamock.specs2.MockContext
import org.specs2.mutable.Specification
import scalaz.zio.clock.Clock
import scalaz.zio.internal.PlatformLive
import scalaz.zio.interop.catz._
import scalaz.zio.{IO, Runtime}

class StockSpec extends Specification with MockContext {

  def setUpTest = {

    val stockDAOMock = mock[StockDAO]
    object ExtServicesTest extends ExtServices with Clock.Live {
      override val stockDao: StockDAO = stockDAOMock
    }

    (stockDAOMock, Runtime(ExtServicesTest, PlatformLive.Default))
  }

  "Stock HTTP Service" should {
    "return 200 and current stock" in {
      val (stockDAOMock, testRuntime) = setUpTest
      val request = Request[STask](Method.GET, uri"""/stock/1""")
      val expectedIO: IO[StockDBAccessError, Stock] = IO.fromEither(Right(Stock(1,10)))
      (stockDAOMock.currentStock _).expects(1).returning(expectedIO)
      val stockResponse = testRuntime.unsafeRun(HTTPService.routes.orNotFound.run(request))
      stockResponse.status must beEqualTo(Status.Ok)
      testRuntime.unsafeRun(stockResponse.as[String]) must beEqualTo("""{"id":1,"value":10}""")
    }

    "return 200 and updated stock" in {
      val (stockDAOMock, testRuntime) = setUpTest
      val request = Request[STask](Method.PUT, uri"""/stock/1/5""")
      (stockDAOMock.updateStock _).expects(1,5).returning(IO.fromEither(Right(Stock(1,15))))
      (stockDAOMock.currentStock _).expects(1).returning(IO.fromEither(Right(Stock(1,5))))
      val stockResponse = testRuntime.unsafeRun(HTTPService.routes.orNotFound.run(request))
      stockResponse.status must beEqualTo(Status.Ok)
      testRuntime.unsafeRun(stockResponse.as[String]) must beEqualTo("""{"id":1,"value":15}""")
    }

    "return error" in {
      val (stockDAOMock, testRuntime) = setUpTest
      val request = Request[STask](Method.GET, uri"""/stock/1""")
      (stockDAOMock.currentStock _).expects(1).returning(IO.fromEither(Right(Stock(1, 0))))
      val stockResponse = testRuntime.unsafeRun(HTTPService.routes.orNotFound.run(request))
      stockResponse.status must beEqualTo(Status.Conflict)
      testRuntime.unsafeRun(stockResponse.as[String]) must beEqualTo("""{"Error":"Stock is empty"}""")
    }
  }
}
