package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom._

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

  def logic(sources: Sources): Sinks = {
    val domDriver = drivers("dom").asInstanceOf[DomDriver]
    val clicks$ = domDriver.selectEvent("#app", "click")
    var counter = 0

    Map(
      "dom" -> {
        clicks$.startWith(null).map(_ => {
          counter += 1
          Div(id = "app", children = Seq(Text(s"hello from scycle - clicks=$counter")))
        })
      }
    )
  }

}
