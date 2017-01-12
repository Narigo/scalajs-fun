package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom._
import com.campudus.scycle.http.HttpDriver
import org.scalajs.dom.Event

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
    "dom" -> new DomDriver,
    "http" -> new HttpDriver
  )

  def logic(sources: Sources): Sinks = {
    val domDriver = drivers("dom").asInstanceOf[DomDriver]
    val clicks$ = domDriver.selectEvent("#app", "click")
    val http = drivers("http").asInstanceOf[HttpDriver]
    var counter = 0

    Map(
      "dom" -> {
        clicks$
          .startWith(null)
          .map(side((_: Event) => counter += 1))
          .combineLatest(http.lastResponse$)
          .map(eventUser => {
            Div(id = "app", children = Seq(Text(s"Hello from Scycle - clicks=$counter, user=${eventUser._2}")))
          })
      },
      "http" -> {
        clicks$
          .startWith(null)
          .flatMap(_ => {
            http.requestUser(Math.floor(Math.random() * 10) + 1)
          })
      }
    )
  }

}
