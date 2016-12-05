package com.campudus.scycle

import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom._
import org.scalajs.dom.Event
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

  val drivers: DriversDefinition = Map[String, DriverFunction[_, _]](
    "dom" -> new DomDriver
  )

  def logic(drivers: Sources): Sinks = {
    println(s"ScycleApp.logic($drivers)")
    val domDriver$ = drivers("dom").asInstanceOf[Subject[DomDriver]]

    println(s"ScycleApp.logic($drivers):changeWeight$$")
    val changeWeight$ = domDriver$.flatMap(
      _.selectEvent(".weight", "input")
        .map(ev => {
          println("ScycleApp.changeWeight$:map event")
          val value = ev.srcElement.asInstanceOf[js.Dynamic].value
          println("got a weight input event", value)
          value.asInstanceOf[Double]
        })
        .startWith(0.0)
    )

    println(s"ScycleApp.logic($drivers):changeHeight$$")
    val changeHeight$ = domDriver$.flatMap(
      _.selectEvent(".height", "input")
        .map(ev => {
          println("ScycleApp.changeHeight$:map event")
          val value = ev.srcElement.asInstanceOf[js.Dynamic].value
          println("got a height input event", value)
          value.asInstanceOf[Double]
        })
        .startWith(1.0)
    )

    println(s"ScycleApp.logic($drivers):state$$")
    val state$ = changeWeight$.combineLatest(changeHeight$).map({
      case (weight, height) =>
        println("ScycleApp.state$:map weight/height")
        val heightMeters = height * 0.01
        val bmi = Math.round(weight / (heightMeters * heightMeters))
        println(s"bmi is $bmi")
        (weight, height, bmi)
    }: ((Double, Double)) => (Double, Double, Double))
      .startWith((0.0, 1.0, 0.0))

    println(s"ScycleApp.logic($drivers):return Map")
    Map(
      "dom" -> {
        state$.map({
          case (weight, height, bmi) =>
            println(s"weight=$weight, height=$height")
            Div(
              id = "app", children = Seq(
                Div(
                  children = Seq(
                    Label(children = Seq(Text(s"Weight: $weight kg"))),
                    Input(className = "weight", kind = "range", value = s"$weight")
                  )
                ),
                Div(
                  children = Seq(
                    Label(children = Seq(Text(s"Height: $height cm"))),
                    Input(className = "height", kind = "range", value = s"$height")
                  )
                ),
                H1(children = Seq(Text(s"BMI is $bmi")))
              )
            )
        }: ((Double, Double, Double)) => Hyperscript)
      }
    )
  }

}
