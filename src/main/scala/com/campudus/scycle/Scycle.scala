package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: Map[String, Driver] => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Driver]
         ): Unit = {

    println("run method")

    println(s"init sinks")
    val proxies = drivers.foldLeft(Map[String, (Driver, Subject[_])]())({
      case (proxyMap, (key, driverFn)) =>
        val proxy = createProxy(driverFn)
        val driver = driverFn(proxy)
        proxyMap + (key -> (driver, proxy))
    })

    val sinks = mainFn(proxies.foldLeft(Map[String, Driver]()) {
      case (acc, (key, driverAndProxy)) =>
        acc + (key -> driverAndProxy._1)
    })

    drivers.foreach {
      case (key, driverFn) =>
        wireProxyToSink(sinks(key), key, proxies)
        drivers(key)(sinks(key))
    }
  }

  def wireProxyToSink[T](source: Observable[T], key: String, proxies: Map[String, (Driver, Subject[_])]): Unit = {
    val proxy = proxies(key)._2.asInstanceOf[Subject[T]]
    source.subscribe({ ev =>
      proxy.next(ev)
    })
  }

  def createProxy[T](driverFn: Observable[T] => Driver): Subject[T] = Subject[T]()

}
