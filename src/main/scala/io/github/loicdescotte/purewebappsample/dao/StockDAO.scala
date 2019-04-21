package io.github.loicdescotte.purewebappsample.dao

import cats.effect.IO
import doobie.implicits._
import io.github.loicdescotte.purewebappsample.IOTransactor
import io.github.loicdescotte.purewebappsample.model.{StockDBAccessError, Stock, StockError}


/**
  * The methods in this class are pure functions
  * They can describe how to interact with the database (select, insert, ...)
  * But as IO is lazy, no side effect will be executed here
  *
  * @param xa
  */
class StockDAO(val xa: IOTransactor) {

  def currentStock(stockId: Int): IO[Either[StockDBAccessError, Stock]] = {
    val stockDatabaseResult = sql"""
      SELECT * FROM stock where id=$stockId
     """.query[Stock].unique.transact(xa).attempt

    stockDatabaseResult.map(withStockErrorManagement)
  }

  def updateStock(stockId: Int, updateValue: Int): IO[Either[StockDBAccessError, Stock]] = {
    val newStockDatabaseResult = for {
      _ <- sql""" UPDATE stock SET value = value + $updateValue where id=$stockId""".update.run
      newStock <- sql"""SELECT * FROM stock where id=$stockId""".query[Stock].unique
    } yield newStock

    newStockDatabaseResult.transact(xa).attempt.map(withStockErrorManagement)
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
