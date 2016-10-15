package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: Map[String, Driver[_]] => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Driver[_]]
         ): Unit = {

    if (drivers.nonEmpty) {
      val sinkProxies = makeSinkProxies(drivers)

      val effects = mainFn(sinkProxies.mapValues(_._2))
      effects.foreach({
        case (key, readEffect$) =>
          readEffect$.map({ ev =>
            println(s"main-in=$ev")
            ev
          }).subscribe({ ev =>
            println(s"got a new event in effect=$ev")
            feedIntoProxy(key, sinkProxies)(ev)
          })
      })
    }
  }

  private def makeSinkProxies(drivers: Map[String, Observable[_] => Driver[_]]): Map[String, (Subject[_], Driver[_])] = {
    drivers.foldLeft(Map[String, (Subject[_], Driver[_])]()) {
      case (m, (key, driver)) => m + (key -> createProxy(driver))
    }
  }

  private def createProxy[A](driverFn: Observable[A] => Driver[_]): (Subject[A], Driver[_]) = {
    val proxy = Subject[A]()
    (proxy, driverFn(proxy))
  }

  private def feedIntoProxy[A](key: String, proxies: Map[String, (Subject[_], Driver[_])])(event: A): Unit = {
    proxies(key)._1.asInstanceOf[Subject[A]].next(event)
  }

}
