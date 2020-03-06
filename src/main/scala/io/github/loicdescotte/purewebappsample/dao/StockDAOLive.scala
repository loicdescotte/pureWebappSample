package io.github.loicdescotte.purewebappsample.dao

import doobie.implicits._
import io.github.loicdescotte.purewebappsample.{ExtServices, IOTransactor}
import io.github.loicdescotte.purewebappsample.model.{Stock, StockDBAccessError, StockError, StockNotFound}
import zio.IO
import zio.interop.catz._


/**
  * The methods in this class are pure functions
  * They can describe how to interact with the database (select, insert, ...)
  * But as IO is lazy, no side effect will be executed here
  *
  * @param xa
  */
class StockDAOLive(val xa: IOTransactor) extends ExtServices.StockDAO.Service {

  override def currentStock(stockId: Int): IO[StockError, Stock] = {
    val stockDatabaseResult = sql"""
      SELECT * FROM stock where id=$stockId
     """.query[Stock].option

    stockDatabaseResult.transact(xa).mapError(StockDBAccessError)
    .flatMap{
      case Some(stock) => IO.succeed(stock)
      case None => IO.fail(StockNotFound)
    }
  }

  override  def updateStock(stockId: Int, updateValue: Int): IO[StockError, Stock] = {
    val newStockDatabaseResult = for {
      _ <- sql""" UPDATE stock SET value = value + $updateValue where id=$stockId""".update.run
      newStock <- sql"""SELECT * FROM stock where id=$stockId""".query[Stock].unique
    } yield newStock

    newStockDatabaseResult.transact(xa).mapError(StockDBAccessError)
  }
}
