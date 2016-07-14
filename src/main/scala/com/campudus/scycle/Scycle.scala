package com.campudus.scycle

import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

object Scycle {

  type LogicOutput = Rx[String]
  type DriverOutput = Var[MouseEvent]

  def run(
           mainFn: (collection.mutable.Map[String, DriverOutput]) => Map[String, LogicOutput],
           drivers: Map[String, (LogicOutput) => DriverOutput]
         )(implicit ctx: Ctx.Owner): Unit = {
    val proxySources = collection.mutable.Map[String, DriverOutput]()
    val sinks = mainFn(proxySources)
    drivers foreach {
      case (key, fn) =>
        val outRx = fn(sinks(key))
        outRx.trigger {
          proxySources(key)() = outRx.now
        }
    }
  }

}
