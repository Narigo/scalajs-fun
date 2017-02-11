package com.campudus.scycle.examples

import com.campudus.scycle.Scycle._
import com.campudus.scycle._
import com.campudus.scycle.dom.DomDriver.makeDomDriver
import com.campudus.scycle.dom._
import rxscalajs.Observable

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    println("main export")

    Scycle.run(logic, drivers)
  }

  val drivers: DriversDefinition = Map[String, Driver[_]](
    "dom" -> makeDomDriver("#app")
  )

  def logic(sources: Sources): Sinks = {
    val change$ = intent(sources("dom").asInstanceOf[DomDriver])
    val value$ = model(change$)
    val vtree$ = view(value$)

    Map(
      "dom" -> vtree$
    )
  }

  def intent(domDriver: DomDriver): Observable[Int] = {
    val change$ = domDriver
      .selectEvent(".slider", "input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)

    change$
  }

  def model(change$: Observable[Int]): Observable[Int] = {
    change$.startWith(70)
  }

  def view(value$: Observable[Int]): Observable[Hyperscript] = {
    value$.map(value => {
      Div(id = "app", className = "labeled-slider", children = List(
        Label(children = List(Text(s"Weight: $value kg"))),
        Input(className = "slider", options = List(
          "type" -> "range", "min" -> s"40", "max" -> s"170", "value" -> s"$value"
        ))
      ))
    })
  }

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

}
