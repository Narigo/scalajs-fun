package com.campudus.scycle

import com.campudus.scycle.dom._
import com.campudus.scycle.http.{HttpDriver, NonRequest, Request}
import rxscalajs.Observable

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    println("main export")

    Scycle.run(logic, Map(
      "dom" -> ((obs: Observable[_]) => DomDriver(obs.asInstanceOf[Observable[Hyperscript]])),
      "http" -> ((obs: Observable[_]) => HttpDriver(obs.asInstanceOf[Observable[Request]]))
    ))
  }

  def logic(drivers: Map[String, Driver]): Map[String, Observable[_]] = {
    println("in logic")
    val domDriver = drivers("dom").asInstanceOf[DomDriver]
//    val clicks$ = domDriver.selectEvents("#app", "click")

    println("test?")
    Map(
      "dom" -> Observable.just("hello") ,//clicks$.map(e => println("clicked!")),
      "http" -> Observable.just(NonRequest)
    )
  }

}
