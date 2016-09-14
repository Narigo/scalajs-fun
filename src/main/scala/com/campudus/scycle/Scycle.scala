package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: (Map[String, Observable[_]]) => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Observable[_]]
         ): Unit = {

    println("run method")

    println(s"init sinks")
    val proxies = drivers.foldLeft(Map[String, Subject[_]]())({
      case (proxyMap, (key, driverFn)) => proxyMap + createProxy(key, driverFn)
    })

    val sinks = mainFn(proxies)
    drivers.foreach {
      case (key, driverFn) => wireProxyToSink(drivers(key)(sinks(key)), key, proxies)
    }
  }

  def wireProxyToSink[T](source: Observable[T], key: String, proxies: Map[String, Subject[_]]): Unit = {
    val proxy = proxies(key).asInstanceOf[Subject[T]]
    source.subscribe({ ev =>
      proxy.next(ev)
    })
  }

  def createProxy[T](key: String, driverFn: Observable[T] => Observable[_]): (String, Subject[T]) = key -> Subject[T]()
}
