package io.github.loicdescotte.purewebappsample

import cats.effect.IO
import io.github.loicdescotte.purewebappsample.dao.StockDAO
import io.github.loicdescotte.purewebappsample.model.Stock
import org.http4s._
import org.http4s.syntax.kleisli._
import org.scalamock.specs2.MockContext
import org.specs2.mutable.Specification

class StockSpec extends Specification {

  "Stock HTTP Service" should {
    "return 200 and current stock" in new MockContext {
      val request = Request[IO](Method.GET, Uri.uri("/stock/1"))
      val databaseAccess = mock[StockDAO]
      (databaseAccess.currentStock _).expects(1).returning(IO(Right(Stock(1,10))))
      val stockResponse = HTTPService(databaseAccess).routes.orNotFound.run(request).unsafeRunSync
      stockResponse.status must beEqualTo(Status.Ok)
      stockResponse.as[String].unsafeRunSync() must beEqualTo("""{"id":1,"value":10}""")
    }

    "return 200 and updated stock" in new MockContext {
      val request = Request[IO](Method.PUT, Uri.uri("/stock/1/5"))
      val databaseAccess = mock[StockDAO]
      (databaseAccess.updateStock _).expects(1,5).returning(IO(Right(Stock(1,15))))
      (databaseAccess.currentStock _).expects(1).returning(IO(Right(Stock(1,5))))
      val stockResponse = HTTPService(databaseAccess).routes.orNotFound.run(request).unsafeRunSync
      stockResponse.status must beEqualTo(Status.Ok)
      stockResponse.as[String].unsafeRunSync() must beEqualTo("""{"id":1,"value":15}""")
    }

    "return error" in new MockContext {
      val request = Request[IO](Method.GET, Uri.uri("/stock/1"))
      val databaseAccess = mock[StockDAO]
      //set stock to zero
      (databaseAccess.currentStock _).expects(1).returning(IO(Right(Stock(1, 0))))
      val stockResponse = HTTPService(databaseAccess).routes.orNotFound.run(request).unsafeRunSync
      stockResponse.status must beEqualTo(Status.Conflict)
      stockResponse.as[String].unsafeRunSync() must beEqualTo("""{"Error":"java.lang.Exception: Stock is empty"}""")
    }
  }
}
