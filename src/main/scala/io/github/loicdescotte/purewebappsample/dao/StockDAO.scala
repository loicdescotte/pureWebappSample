package io.github.loicdescotte.purewebappsample.dao

import cats.effect.IO
import doobie.implicits._
import io.github.loicdescotte.purewebappsample.IOTransactor
import io.github.loicdescotte.purewebappsample.model.{StockDBAccessError, Stock, StockError}


/**
  * The methods in this class are pure functions
  * They can describe how to interact with the database (select, insert, ...)
  * But as IO is lazy, no side effect will be executed here
  * @param xa
  */
class StockDAO(val xa: IOTransactor) {

  def currentStock(stockId: Int): IO[Either[StockError, Stock]] = {
    val stockDatabaseResult = sql"""
      SELECT * FROM stock where id=$stockId
     """.query[Stock].unique.transact(xa).attempt

    stockDatabaseResult.map(withStockErrorManagement)
  }

  def updateStock(stockId: Int, updateValue: Int): IO[Either[StockError, Int]] = {
    val stockDatabaseResult = sql"""
      UPDATE stock
      SET value = $updateValue
      where id=$stockId
     """.update.run.transact(xa).attempt

    stockDatabaseResult.map(withStockErrorManagement)
  }

  private def withStockErrorManagement[A](stockDatabaseResult: Either[Throwable, A]): Either[StockDBAccessError, A] = {
    stockDatabaseResult.fold(
      // if left, use typed errors
      throwable => Left(StockDBAccessError(throwable.getMessage)),
      // else there is nothing to do
      Right(_)
    )
  }
}
