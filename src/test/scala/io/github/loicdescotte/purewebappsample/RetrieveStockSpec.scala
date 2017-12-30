package io.github.loicdescotte.purewebappsample

import cats.effect.IO
import io.github.loicdescotte.purewebappsample.dao.StockDAO
import io.github.loicdescotte.purewebappsample.model.Stock
import org.http4s._
import org.http4s.implicits._
import org.scalamock.specs2.MockContext
import org.specs2.mutable.Specification

class RetrieveStockSpec extends Specification {

  "Stock HTTP Service" should {
    "return 200" in new MockContext {
      val request = Request[IO](Method.GET, Uri.uri("/stock"))
      val databaseAccess = mock[StockDAO]
      (databaseAccess.currentStock _).expects.returning(IO(Right(Stock(10))))
      val stockResponse = HTTPService.service(databaseAccess).orNotFound(request).unsafeRunSync()
      stockResponse.status must beEqualTo(Status.Ok)
      stockResponse.as[String].unsafeRunSync() must beEqualTo("""{"stock":10}""")
    }

    "return error" in new MockContext {
      val request = Request[IO](Method.GET, Uri.uri("/stock"))
      val databaseAccess = mock[StockDAO]
      //set stock to zero
      (databaseAccess.currentStock _).expects.returning(IO(Right(Stock(0))))
      val stockResponse = HTTPService.service(databaseAccess).orNotFound(request).unsafeRunSync()
      stockResponse.status must beEqualTo(Status.Conflict)
      stockResponse.as[String].unsafeRunSync() must beEqualTo("""{"stock":"Stock is empty"}""")
    }
  }
}
