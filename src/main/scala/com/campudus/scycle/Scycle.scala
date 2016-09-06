package com.campudus.scycle

import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

object Scycle {

  type DriverOutput = Var[MouseEvent]

  def run(
           mainFn: (collection.Map[String, Driver]) => Rx[collection.Map[String, _]],
           drivers: collection.Map[String, Ctx.Owner => Rx[_] => Driver]
         )(implicit ctx: Ctx.Owner): Unit = {

    println(s"owner in Scycle.run = $ctx")
    val realDrivers = drivers.mapValues(fn => fn(ctx)(Rx {null}))
    val sinks = mainFn(realDrivers)
    drivers.foreach {
      case (key, fn) => fn(ctx)(Rx {
        val map = sinks()
        map(key)
      })
    }

  }

}
