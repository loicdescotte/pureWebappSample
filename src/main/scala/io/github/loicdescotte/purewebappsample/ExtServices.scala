package io.github.loicdescotte.purewebappsample

import doobie.util.transactor.Transactor
import io.github.loicdescotte.purewebappsample.dao.{StockDAO, StockDAOLive}
import scalaz.zio.Task
import scalaz.zio.clock.Clock
import scalaz.zio.interop.catz._


/**
  * External services
  */
trait ExtServices extends Clock {
  val stockDao: StockDAO
}

object ExtServicesLive extends ExtServices with Clock.Live  {

  val xa = Transactor.fromDriverManager[Task](
    "org.h2.Driver",
    "jdbc:h2:mem:poc;INIT=RUNSCRIPT FROM 'src/main/resources/sql/create.sql'"
    , "sa", ""
  )
  override val stockDao: StockDAO = new StockDAOLive(xa)
}
