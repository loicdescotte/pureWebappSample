package io.github.loicdescotte

import scalaz.zio.Task
import doobie.util.transactor.Transactor.Aux

package object purewebappsample {

  type IOTransactor = Aux[Task, Unit]

}
