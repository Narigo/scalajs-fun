package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom.DomDriver.makeDomDriver
import com.campudus.scycle.dom._
import com.campudus.scycle.http.HttpDriver.makeHttpDriver
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
    "dom" -> makeDomDriver("#app"),
    "http" -> makeHttpDriver
  )

  def logic(sources: Sources): Sinks = {
    val (changeWeight$, changeHeight$) = intent(sources("dom").asInstanceOf[DomDriver])
    val state$ = model(changeWeight$, changeHeight$)
    val vtree$ = view(state$)

    Map(
      "dom" -> vtree$
    )
  }

  def intent(domDriver: DomDriver): (Observable[Int], Observable[Int]) = {
    val changeWeight$ = domDriver
      .selectEvent(".weight", "input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)
    val changeHeight$ = domDriver
      .selectEvent(".height", "input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)

    (changeWeight$, changeHeight$)
  }

  def model(changeWeight$: Observable[Int], changeHeight$: Observable[Int]): Observable[(Int, Int, Long)] = {
    changeWeight$.startWith(70)
      .combineLatest(changeHeight$.startWith(170))
      .map((tuple, _) => {
        val (weight, height) = tuple
        val heightMeters = height * 0.01
        val bmi = Math.round(weight / (heightMeters * heightMeters))
        (weight, height, bmi)
      })
  }

  def view(state$: Observable[(Int, Int, Long)]): Observable[Hyperscript] = {
    state$.map(triple => {
      val (weight, height, bmi) = triple
      Div("app", children = List(
        Div(children = List(
          Label(children = List(Text(s"Weight: $weight kg"))),
          Input(className = "weight", options = List(
            "type" -> "range", "min" -> "40", "max" -> "150", "value" -> s"$weight"
          ))
        )),
        Div(children = List(
          Label(children = List(Text(s"Height: $height cm"))),
          Input(className = "height", options = List(
            "type" -> "range", "min" -> "140", "max" -> "220", "value" -> s"$height"
          ))
        )),
        H1(children = List(Text(s"BMI is $bmi")))
      ))
    })
  }

}
