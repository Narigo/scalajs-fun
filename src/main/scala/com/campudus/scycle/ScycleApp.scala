package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom._
import com.campudus.scycle.http.HttpDriver

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
          .map(click => {
            counter += 1
            click
          })
          .combineLatest(http.lastResponse$)
          .map((evUser, _) => {
            Div(id = "app", children = Seq(Text(s"hello from scycle - clicks=$counter, user=${evUser._2}")))
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
