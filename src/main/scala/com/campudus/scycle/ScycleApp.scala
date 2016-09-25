package com.campudus.scycle

import com.campudus.scycle.dom._
import com.campudus.scycle.http._
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

  val drivers: Map[String, Observable[_] => Driver] = Map(
    "dom" -> DomDriver.apply _
  )

  def logic(drivers: (Map[String, Driver])): Map[String, Observable[_]] = {
    println("called logic")

    Map(
      "dom" -> Observable.just(
        Div(id = "app", children = Seq(
          Div(children = Seq(
            Label(children = Seq(Text("Weight: 00 kg"))),
            Input(className = "weight", kind = "range", value = "")
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
