package io.github.loicdescotte.purewebappsample.dao

import cats.effect.IO
import doobie.implicits._
import io.github.loicdescotte.purewebappsample.IOTransactor
import io.github.loicdescotte.purewebappsample.model.{NonReachableStock, Stock, StockError}


/**
  * The methods in this class are pure functions
  * They can describe how to interact with the database (select, insert, ...)
  * But as IO is lazy, no side effect will be executed here
  * @param xa
  */
class StockDAO(val xa: IOTransactor) {

  def currentStock: IO[Either[StockError, Stock]] = {
    val stockDatabaseResult = sql"""
      SELECT stock FROM stock
     """.query[Stock].unique.transact(xa).attempt

    stockDatabaseResult.map(withStockErrorManagement)
  }

  private def withStockErrorManagement(stockDatabaseResult: Either[Throwable, Stock]): Either[NonReachableStock, Stock] = {
    stockDatabaseResult.fold(
      // if left, use typed errors
      throwable => Left(NonReachableStock(throwable.getMessage)),
      // else there is nothing to do
      Right(_)
    )
  }
}
