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
          driver.map({ in =>
            println(s"driver-in=$in")
            in
          }).subscribe(_ => {})
          m + (key -> (proxy, driver))
      }

      val effects = mainFn(proxyDriverMap.mapValues(_._2))
      effects.foreach({
        case (key, readEffect$) => readEffect$.map({ ev =>
          println(s"effect ev=$ev")
          ev
        }).subscribe(feedIntoProxy(key, proxyDriverMap) _)
      })
    }
  }

  private def createProxy[A](driverFn: Observable[A] => Observable[_]): Subject[A] = Subject[A]()

  private def feedIntoProxy[A](key: String, proxies: Map[String, (Subject[_], Observable[_])])(event: A): Unit = {
    proxies(key)._1.asInstanceOf[Subject[A]].next(event)
  }

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
