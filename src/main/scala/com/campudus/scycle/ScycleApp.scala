package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom._
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

  def logic(drivers: Sources): Sinks = {
    println(s"ScycleApp.logic($drivers)")
    Map(
      "dom" -> {
        Observable.just(Div(id = "app", children = Seq(Text("hello from scycle"))))
      }
    )
  }

}
