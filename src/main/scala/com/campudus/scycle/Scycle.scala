package com.campudus.scycle

import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

object Scycle {

  type DriverOutput = Var[MouseEvent]

  def run(
           mainFn: (collection.Map[String, Driver]) => Rx[collection.Map[String, _]],
           drivers: collection.Map[String, Rx[_] => Driver]
         )(implicit ctx: Ctx.Owner): Unit = {

    val realDrivers = drivers.mapValues(fn => fn(Rx {null}))
    val sinks = mainFn(realDrivers)
    drivers.foreach {
      case (key, fn) => fn(Rx {
        val map = sinks()
        map(key)
      })
    }

  }

}
