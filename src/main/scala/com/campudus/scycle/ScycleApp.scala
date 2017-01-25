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
    val domDriver = sources("dom").asInstanceOf[DomDriver]
    val changeWeight$ = domDriver
      .selectEvent(".weight", "click")
      .map(ev => {
        println("hello weight?")
        ev.target.valueOf()
      }).startWith(70)
    val changeHeight$ = domDriver
      .selectEvent(".height", "input")
      .map(ev => {
        println("hello height?")
        ev.target.valueOf()
      })
      .startWith(170)

    val bmi$ = Observable
      .combineLatest(List(changeWeight$, changeHeight$))(
        seq => {
          println("hello bmi?")
          val weight = seq.head.toString.toInt
          val height = seq(1).toString.toInt
          val heightMeters = height * 0.01
          val bmi = Math.round(weight / (heightMeters * heightMeters))
          bmi
        }
      )
      .startWith(15)

    Map(
      "dom" -> {
        bmi$.map(l => {
          Div("#app", children = List(
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
        })
      }
    )
  }

}
