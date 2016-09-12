package com.campudus.scycle

import com.campudus.scycle.dom.{Div, DomDriver, Hyperscript, Text}
import org.scalajs.dom.Event
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

  val drivers: Map[String, Observable[_] => Observable[_]] = Map(
    "dom" -> (obs => DomDriver.apply(obs.asInstanceOf[Observable[Hyperscript]]))
  )

  def logic(drivers: (Map[String, Observable[_]])): Map[String, Observable[_]] = {
    println("called logic")
    val clicks$ = drivers("dom").asInstanceOf[Observable[Event]]
    Map(
      "dom" -> clicks$
        .map(ev => {
          println("clicked on dom in logic!")
          Div(id = "app", children = Seq(Text(s"hello ${Math.random()}")))
        })
    )
  }
}
