package io.github.loicdescotte.purewebappsample

import doobie.util.transactor.Transactor
import io.github.loicdescotte.purewebappsample.dao.StockDAOLive
import io.github.loicdescotte.purewebappsample.model._
import zio._
import zio.clock.Clock
import zio.interop.catz._

object ExtServices {

  type ExtServices = StockDAO with Clock
  type StockDAO = Has[StockDAO.Service]

  object StockDAO {

    trait Service {
      def currentStock(stockId: Int): IO[StockError, Stock]

      def updateStock(stockId: Int, updateValue: Int): IO[StockError, Stock]
    }

    val live: ZLayer.NoDeps[Nothing, StockDAO] =
      ZLayer.succeed {
        val xa = Transactor.fromDriverManager[Task](
          "org.h2.Driver",
          "jdbc:h2:file:./localdb;INIT=RUNSCRIPT FROM 'src/main/resources/sql/create.sql'"
          , "sa", ""
        )
        new StockDAOLive(xa)
      }
  }

  val extServicesLive = StockDAO.live ++ Clock.live

}