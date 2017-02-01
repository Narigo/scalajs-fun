package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom.DomDriver.makeDomDriver
import com.campudus.scycle.dom._
import com.campudus.scycle.http.HttpDriver.makeHttpDriver
import org.scalajs.dom.Element

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
    val domDriver = sources("dom").asInstanceOf[DomDriver]
    val changeWeight$ = domDriver
      .selectEvent(".weight", "input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toDouble)
      .startWith(70.0)
    val changeHeight$ = domDriver
      .selectEvent(".height", "input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toDouble)
      .startWith(170.0)

    val bmi$ = changeWeight$
      .combineLatest(changeHeight$)
      .map((tuple, _) => {
        val (weight, height) = tuple
        val heightMeters = height * 0.01
        val bmi = Math.round(weight / (heightMeters * heightMeters))
        (weight, height, bmi)
      })

    Map(
      "dom" -> {
        bmi$.map(tuple => {
          val (weight, height, bmi) = tuple
          org.scalajs.dom.window.console.log("mapped bmi", s"$bmi")
          Div("app", children = List(
            Div(children = List(
              Label(children = List(Text(s"Weight: $weight kg"))),
              Input(className = "weight", options = List(
                "type" -> "range", "min" -> "40", "max" -> "150", "value" -> s"$weight"
              ))
            )),
            Div(children = List(
              Label(children = List(Text(s"Height: $height kg"))),
              Input(className = "height", options = List(
                "type" -> "range", "min" -> "140", "max" -> "220", "value" -> s"$height"
              ))
            )),
            H1(children = List(Text(s"BMI is $bmi")))
          ))
        })
      }
    )
  }

}
