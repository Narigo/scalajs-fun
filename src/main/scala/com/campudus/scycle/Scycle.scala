package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: (Map[String, Driver]) => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Driver]
         ): Unit = {

    println("run method")

    val initialDrivers = drivers.mapValues(fn => fn(Observable.just(null)))
    val sinks = mainFn(initialDrivers)

    drivers.foreach {
      case (key, fn) => fn(sinks(key))
    }

  }

}
