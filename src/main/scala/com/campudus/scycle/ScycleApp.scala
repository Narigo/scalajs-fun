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
    val domDriver$ = drivers("dom").asInstanceOf[Subject[DomDriver]]
    val clicks$ = domDriver$.flatMap(driver => driver.selectEvent("#app", "click"))
    var counter = 0

    Map(
      "dom" -> {
        Observable.just(null)/*.concat(clicks$)*/.map(_ => {
          counter += 1
          Div(id = "app", children = Seq(Text(s"hello from scycle - clicks=$counter")))
        })
      }
    )
  }

}
