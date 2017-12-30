package io.github.loicdescotte.purewebappsample.model

case class Stock(value: Int) extends AnyVal

sealed abstract class StockError(message: String) extends Exception(message)
case object EmptyStock extends StockError("Stock is empty")
case class NonReachableStock(message: String) extends StockError(message)

/**
  * Stock business logic
  */
object Stock {
  def validate(stock: Stock): Either[StockError, Stock] = if (stock.value > 0) Right(stock) else Left(EmptyStock)
}