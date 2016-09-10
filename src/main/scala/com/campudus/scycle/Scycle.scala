package com.campudus.scycle

import org.scalajs.dom.Event
import rxscalajs._

object Scycle {

  def run(
           mainFn: (Map[String, Observable[_]]) => Map[String, Observable[_]],
           drivers: Map[String, Observable[_] => Observable[_]]
         ): Unit = {

    println("run method")

    println(s"init sinks")
    val proxyDomSource = Subject[Event]()
    val sinks = mainFn(Map("dom" -> proxyDomSource))
    println(s"some sinks there")

    val domSource = drivers("dom")(sinks("dom")).asInstanceOf[Observable[Event]]
    domSource.subscribe((click: Event) => proxyDomSource.next(click))

    drivers.keys.foreach { key =>
      println(s"init driver with sinks")
      drivers(key)(sinks(key))
    }

  }

}
