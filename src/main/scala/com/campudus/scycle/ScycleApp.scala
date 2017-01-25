package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom.DomDriver.makeDomDriver
import com.campudus.scycle.dom._
import com.campudus.scycle.http.HttpDriver
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
    val domDriver = drivers("dom").asInstanceOf[DomDriver]

    Map(
      "dom" -> {
        Observable.of(
          Div(children = List(
            Div(children = List(
              Label(children = List(Text("Weight: 00kg"))),
              Input(className = "weight", options = List(
                "type" -> "range", "min" -> "40", "max" -> "150", "value" -> "70"
              ))
            )),
            Div(children = List(
              Label(children = List(Text("Height: 00kg"))),
              Input(className = "height", options = List(
                "type" -> "range", "min" -> "140", "max" -> "220", "value" -> "170"
              ))
            )),
            H1(children = List(Text("BMI is 000")))
          ))
        )
      }
    )
  }

}
