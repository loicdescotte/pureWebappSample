package io.github.loicdescotte.purewebappsample

import io.github.loicdescotte.purewebappsample.dao.StockDAO
import io.github.loicdescotte.purewebappsample.model.{Stock, StockDBAccessError}
import org.http4s._
import org.scalamock.specs2.MockContext
import org.specs2.mutable.Specification
import scalaz.zio.interop.catz._
import scalaz.zio.{DefaultRuntime, IO, Task}

class StockSpec extends Specification {

  val zioRuntime = new DefaultRuntime {}

  "Stock HTTP Service" should {
    "return 200 and current stock" in new MockContext {
      val request = Request[Task](Method.GET, uri"""/stock/1""")
      val databaseAccess = mock[StockDAO]
      val expectedIO: IO[StockDBAccessError, Stock] = IO.fromEither(Right(Stock(1,10)))
      (databaseAccess.currentStock _).expects(1).returning(expectedIO)
      val stockResponse = zioRuntime.unsafeRun(HTTPService(databaseAccess).routes.run(request).value).get
      stockResponse.status must beEqualTo(Status.Ok)
      zioRuntime.unsafeRun(stockResponse.as[String]) must beEqualTo("""{"id":1,"value":10}""")
    }

    "return 200 and updated stock" in new MockContext {
      val request = Request[Task](Method.PUT, Uri.uri("/stock/1/5"))
      val databaseAccess = mock[StockDAO]
      (databaseAccess.updateStock _).expects(1,5).returning(IO.fromEither(Right(Stock(1,15))))
      (databaseAccess.currentStock _).expects(1).returning(IO.fromEither(Right(Stock(1,5))))
      val stockResponse = zioRuntime.unsafeRun(HTTPService(databaseAccess).routes.run(request).value).get
      stockResponse.status must beEqualTo(Status.Ok)
      zioRuntime.unsafeRun(stockResponse.as[String]) must beEqualTo("""{"id":1,"value":15}""")
    }

    "return error" in new MockContext {
      val request = Request[Task](Method.GET, uri"""/stock/1""")
      val databaseAccess = mock[StockDAO]
      //set stock to zero
      (databaseAccess.currentStock _).expects(1).returning(IO.fromEither(Right(Stock(1, 0))))
      val stockResponse = zioRuntime.unsafeRun(HTTPService(databaseAccess).routes.run(request).value).get
      stockResponse.status must beEqualTo(Status.Conflict)
      zioRuntime.unsafeRun(stockResponse.as[String]) must beEqualTo("""{"Error":"java.lang.Exception: Stock is empty"}""")
    }
  }
}
