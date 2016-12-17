package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom._
import org.scalajs.dom.raw.Event
import rxscalajs._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    println("main export")

    Scycle.run(logic, drivers)
  }

  val drivers: DriversDefinition = Map[String, DriverFunction[_, _]](
    "dom" -> new DomDriver
  )

  def logic(sources: Sources): Sinks = {
    println(s"ScycleApp.logic($sources)")
    val domEvents$ = sources("dom").asInstanceOf[Subject[Event]]
    println(s"ScycleApp.logic($sources):domEvents$$ = ${domEvents$}")
    val clicks$ = drivers("dom").asInstanceOf[DomDriver].selectEvent("#app", "click")
    println(s"ScycleApp.logic:feed domEvents$$ into clicks$$")
    clicks$.subscribe(domEvents$)
    var counter = 0

    Map(
      "dom" -> {
        println("ScycleApp.return:before domEvents$")
        val cObs = clicks$.map(ev => {
          println(s"ScycleApp.return:in domEvents$$:$$ev")
          counter += 1
          Div(id = "app", children = Seq(Text(s"hello from scycle - clicks=$counter")))
        })
        cObs.subscribe(_ => println("don't care about cObs ?"))
        cObs
      }
    )
  }

}
