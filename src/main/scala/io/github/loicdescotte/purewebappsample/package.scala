package io.github.loicdescotte

import doobie.util.transactor.Transactor.Aux
import io.github.loicdescotte.purewebappsample.Dependencies.ExtServices
import org.http4s.Response
import zio.{RIO, Task, ZIO}

package object purewebappsample {

  type IOTransactor = Aux[Task, Unit]

  type SIO[E, A] = ZIO[ExtServices, E, A]

  type STask[A] = RIO[ExtServices, A]

  type SResponse = STask[Response[STask]]

}
