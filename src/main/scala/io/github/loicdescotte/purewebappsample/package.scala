package io.github.loicdescotte

import doobie.util.transactor.Transactor.Aux
import scalaz.zio.{Task, TaskR, ZIO}

package object purewebappsample {

  type IOTransactor = Aux[Task, Unit]

  type SIO[E, A] = ZIO[ExtServices, E, A]

  type STask[A] = TaskR[ExtServices, A]

}
