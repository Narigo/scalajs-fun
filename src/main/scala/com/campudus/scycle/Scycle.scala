package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: Map[String, Observable[_]] => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Observable[_]]
         ): Unit = {

    if (drivers.nonEmpty) {
      val proxyDriverMap = drivers.foldLeft(Map[String, (Subject[_], Observable[_])]()) {
        case (m, (key, driverFn)) =>
          val proxy = createProxy(driverFn)
          val driver = driverFn(proxy)
          m + (key -> (proxy, driver))
      }

      mainFn(proxyDriverMap.mapValues(_._2))
    }
  }

  private def createProxy[A](driverFn: Observable[A] => Observable[_]): Subject[A] = Subject[A]()

  //  def run(
  //           mainFn: Map[String, Observable[_]] => Map[String, Observable[_]],
  //           drivers: Map[String, Observable[_] => Observable[_]]
  //         ): Unit = {
  //
  //    println("run method")
  //
  //    println(s"init sinks")
  //    val proxies = drivers.foldLeft(Map[String, Subject[_]]())({
  //      case (proxyMap, (key, driverFn)) =>
  //        val proxy = Subject()
  //        proxyMap + (key -> proxy)
  //    })
  //
  //    val sinks = mainFn(proxies)
  //
  //    drivers.foreach {
  //      case (key, driverFn) =>
  //        wireProxyToSink(driverFn(sinks(key)), key, proxies)
  //    }
  //  }
  //
  //  def wireProxyToSink[T](source: Observable[T], key: String, proxies: Map[String, Subject[_]]): Unit = {
  //    println(s"wireProxyToSink $key -> $source")
  //    val proxy = proxies(key).asInstanceOf[Subject[T]]
  //    source.map({ ev =>
  //      println("in wired apply")
  //      proxy.next(ev)
  //    })
  //  }

}
