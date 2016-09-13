package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: (Map[String, Observable[_]]) => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Observable[_]]
         ): Unit = {

    println("run method")

    println(s"init sinks")
    val proxies = drivers.keys.foldLeft(Map[String, Subject[_]]())({ (ps, key) =>
      ps + (key -> Subject[_]())
    })

    val sinks = mainFn(proxies)
    drivers.keys.foreach { key =>
      proxyHelper(drivers(key)(sinks(key)), key, proxies)
    }
  }

  def proxyHelper[T](source: Observable[T], key: String, proxies: Map[String, Subject[_]]): Unit = {
    val proxy = proxies(key).asInstanceOf[Subject[T]]
    source.subscribe({ ev =>
      proxy.next(ev)
    })
  }
}
