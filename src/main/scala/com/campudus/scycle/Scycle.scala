package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: (Map[String, Driver]) => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Driver]
         ): Unit = {

    println("run method")

    val initialDrivers = drivers.mapValues(fn => {
      println(s"init driver with fn $fn")
      val initialObs = Observable.just(null)
      fn(initialObs)
    })
    println(s"init sinks")
    val sinks = mainFn(initialDrivers)
    println(s"some sinks there")

    drivers.foreach {
      case (key, fn) => {
        println(s"init driver with sinks")
        fn(sinks(key))
      }
    }

  }

}
