package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: Map[String, Driver] => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Driver]
         ): Unit = {

    println("run method")

    println(s"init sinks")
    val proxies = drivers.foldLeft(Map[String, Subject[_]]())({
      case (proxyMap, (key, driverFn)) => proxyMap + createProxy(key, driverFn)
    })

    val sinks = mainFn(proxies.foldLeft(Map[String, Driver]()) {
      case (acc, (key, proxy)) => acc + (key -> drivers(key)(proxy))
    })

    drivers.foreach {
      case (key, driverFn) => drivers(key)(sinks(key))
    }
  }

  def wireProxyToSink[T](source: Observable[T], key: String, proxies: Map[String, Subject[_]]): Unit = {
    val proxy = proxies(key).asInstanceOf[Subject[T]]
    source.subscribe({ ev =>
      proxy.next(ev)
    })
  }

  def createProxy[T](key: String, driverFn: Observable[T] => Driver): (String, Subject[T]) = key -> Subject[T]()

}
