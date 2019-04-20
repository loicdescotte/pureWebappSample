package io.github.loicdescotte.purewebappsample.model

case class Stock(id:Int, value: Int)

sealed abstract class StockError(message: String) extends Exception(message)
case object EmptyStock extends StockError("Stock is empty")
case class StockDBAccessError(message: String) extends StockError(message)

/**
  * Stock business logic
  */

object Stock {
  def validate(stock: Stock): Either[StockError, Stock] = if (stock.value > 0) Right(stock) else Left(EmptyStock)
}