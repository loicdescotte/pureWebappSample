package io.github.loicdescotte

import scalaz.zio.{Task, TaskR, ZIO}
import doobie.util.transactor.Transactor.Aux

package object purewebappsample {

  type IOTransactor = Aux[Task, Unit]

  type SIO[E, A] = ZIO[ExtServices, E, A]

  type STask[A] = TaskR[ExtServices, A]

}
