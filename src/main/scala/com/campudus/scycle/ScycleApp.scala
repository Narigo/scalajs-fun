package com.campudus.scycle

import com.campudus.scycle.dom._
import rxscalajs._

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    println("main export")

    Scycle.run(logic, drivers)
  }

  val drivers: Map[String, Observable[_] => Driver] = Map(
    "dom" -> DomDriver.apply _
  )

  def logic(drivers: (Map[String, Driver])): Map[String, Observable[_]] = {
    println("called logic")
    val changeWeight$ = drivers("dom").asInstanceOf[DomDriver]
      .selectEvent("#app", "input")
      .map(ev => {
        val value = ev.srcElement.asInstanceOf[js.Dynamic].value
        org.scalajs.dom.console.log("got an input event", value)
        value.toString
      })
      .startWith("0")

    Map(
      "dom" -> changeWeight$.map(weight =>
        Div(id = "app", children = Seq(
          Div(children = Seq(
            Label(children = Seq(Text(s"Weight: $weight kg"))),
            Input(className = "weight", kind = "range", value = weight)
          )),
          Div(children = Seq(
            Label(children = Seq(Text("Height: 00 kg"))),
            Input(className = "height", kind = "range", value = "")
          )),
          H1(children = Seq(Text("BMI is 0")))
        ))
      )
    )
  }
}
