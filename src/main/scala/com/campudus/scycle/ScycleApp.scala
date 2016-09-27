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
    val domDriver = drivers("dom").asInstanceOf[DomDriver]
    val changeWeight$ = domDriver
      .selectEvent(".weight", "input")
      .map(ev => {
        val value = ev.srcElement.asInstanceOf[js.Dynamic].value
        org.scalajs.dom.console.log("got an weight input event", value)
        value.toString
      })
      .startWith("0")
    val changeHeight$ = domDriver
      .selectEvent(".height", "input")
      .map(ev => {
        val value = ev.srcElement.asInstanceOf[js.Dynamic].value
        org.scalajs.dom.console.log("got an height input event", value)
        value.toString
      })
      .startWith("0")

    val state$ = Observable.combineLatest(List(changeWeight$, changeHeight$))({
      case weight :: height :: Nil => (weight, height, "0")
    }).startWith(("0", "0", "0"))

    Map(
      "dom" -> state$.map({
        case (weight, height, bmi) =>
          println(s"weight=$weight, height=$height")
          Div(id = "app", children = Seq(
            Div(children = Seq(
              Label(children = Seq(Text(s"Weight: $weight kg"))),
              Input(className = "weight", kind = "range", value = weight)
            )),
            Div(children = Seq(
              Label(children = Seq(Text(s"Height: $height cm"))),
              Input(className = "height", kind = "range", value = height)
            )),
            H1(children = Seq(Text(s"BMI is $bmi")))
          ))
      }: ((String, String, String)) => Hyperscript)
    )
  }
}
