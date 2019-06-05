package io.github.loicdescotte.purewebappsample.dao

import doobie.implicits._
import io.github.loicdescotte.purewebappsample.IOTransactor
import io.github.loicdescotte.purewebappsample.model.{Stock, StockDBAccessError}
import scalaz.zio.IO
import scalaz.zio.interop.catz._

/**
  * The methods in this class are pure functions
  * They can describe how to interact with the database (select, insert, ...)
  * But as IO is lazy, no side effect will be executed here
  *
  * @param xa
  */
class StockDAO(val xa: IOTransactor) {

  def currentStock(stockId: Int): IO[StockDBAccessError, Stock] = {
    sql"""
      SELECT * FROM stock where id=$stockId
     """.query[Stock].unique.transact(xa).mapError(StockDBAccessError)
  }

  def updateStock(stockId: Int, updateValue: Int): IO[StockDBAccessError, Stock] = {
    val newStockDatabaseResult = for {
      _ <- sql""" UPDATE stock SET value = value + $updateValue where id=$stockId""".update.run
      newStock <- sql"""SELECT * FROM stock where id=$stockId""".query[Stock].unique
    } yield newStock

    newStockDatabaseResult.transact(xa).mapError(StockDBAccessError)
  }
}
