package com.campudus.scycle

import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

object Scycle {

  type LogicOutput = Rx[_]
  type DriverOutput = Var[MouseEvent]

  def run(
           mainFn: (collection.Map[String, Driver]) => collection.Map[String, LogicOutput],
           drivers: collection.Map[String, LogicOutput => Driver]
         )(implicit ctx: Ctx.Owner): Unit = {
    val realDrivers = drivers.mapValues(fn => {
      fn(Rx {
        null
      })
    })
    val sinks = mainFn(realDrivers)
    drivers foreach {
      case (key, fn) =>
        fn(sinks(key))
    }
  }

}
