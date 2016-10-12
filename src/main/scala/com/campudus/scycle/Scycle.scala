package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: Map[String, Driver[_]] => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Driver[_]]
         ): Unit = {

    if (drivers.nonEmpty) {
      val proxyDriverMap = drivers.foldLeft(Map[String, (Subject[_], Driver[_])]()) {
        case (m, (key, driverFn)) =>
          m + (key -> createProxy(driverFn))
      }

      val effects = mainFn(proxyDriverMap.mapValues(_._2))
      effects.foreach({
        case (key, readEffect$) => readEffect$.map({ ev =>
          println(s"main-in=$ev")
          ev
        }).subscribe({ev =>
          println(s"got a new event in effect=$ev")
          feedIntoProxy(key, proxyDriverMap)(ev)
        })
      })
    }
  }

  private def createProxy[A](driverFn: Observable[A] => Driver[_]): (Subject[A], Driver[_]) = {
    val proxy = Subject[A]()
    (proxy, driverFn(proxy))
  }

  private def feedIntoProxy[A](key: String, proxies: Map[String, (Subject[_], Driver[_])])(event: A): Unit = {
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
