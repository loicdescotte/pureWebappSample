package io.github.loicdescotte

import cats.effect.IO
import doobie.util.transactor.Transactor.Aux

package object purewebappsample {

  type IOTransactor = Aux[IO, Unit]

}
