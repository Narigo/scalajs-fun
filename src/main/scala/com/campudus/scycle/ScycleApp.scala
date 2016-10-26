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

    //    Scycle.run(logic, drivers)
  }

  val drivers: Map[String, Observable[_] => Observable[_]] = Map(
    "dom" -> DomDriver.apply _
  )

  def logic(drivers: (Map[String, Observable[_]])): Map[String, Observable[_]] = {
    println("called logic")
    val domDriver$ = drivers("dom").asInstanceOf[Observable[DomDriver]]
    val changeWeight$ = domDriver$.flatMap(
      _.selectEvent(".weight", "input")
        .map(ev => {
          org.scalajs.dom.console.log("hello in .weight map")
          val value = ev.srcElement.asInstanceOf[js.Dynamic].value
          org.scalajs.dom.console.log("got a weight input event", value)
          value.asInstanceOf[Double]
        })
        .startWith(0.0)
    )

    val changeHeight$ = domDriver$.flatMap(
      _.selectEvent(".height", "input")
        .map(ev => {
          org.scalajs.dom.console.log("hello in .height map")
          val value = ev.srcElement.asInstanceOf[js.Dynamic].value
          org.scalajs.dom.console.log("got a height input event", value)
          value.asInstanceOf[Double]
        })
        .startWith(1.0)
    )

    val state$ = changeWeight$.combineLatest(changeHeight$).map({
      case (weight, height) =>
        org.scalajs.dom.console.log("hello in combineLatest")
        val heightMeters = height * 0.01
        val bmi = Math.round(weight / (heightMeters * heightMeters))
        org.scalajs.dom.console.log("bmi is", bmi)
        (weight, height, bmi)
    }: ((Double, Double)) => (Double, Double, Double))
      .startWith((0.0, 1.0, 0.0))

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
