package com.campudus.scycle.examples

import com.campudus.scycle.Scycle._
import com.campudus.scycle._
import com.campudus.scycle.dom.DomDriver.makeDomDriver
import com.campudus.scycle.dom._
import rxscalajs.Observable

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    println("main export")

    Scycle.run(logic, drivers)
  }

  val drivers: DriversDefinition = Map[String, Driver[_]](
    "dom" -> makeDomDriver("#app")
  )

  def logic(sources: Sources): Sinks = {
    val heightSliderProps = Props("Height", "cm", 140, 220, 170)
    val weightSliderProps = Props("Weight", "kg", 40, 150, 70)
    val heightSinks = LabeledSlider(sources + ("props" -> makeSliderPropsDriver(heightSliderProps)))
    val weightSinks = LabeledSlider(sources + ("props" -> makeSliderPropsDriver(weightSliderProps)))

    val vtree$ = weightSinks("dom")
      .combineLatest(heightSinks("dom"))
      .map(tuple => {
        val (weightVTree$, heightVTree$) = tuple.asInstanceOf[(Hyperscript, Hyperscript)]
        Div(id = "app", children = List(
          weightVTree$,
          heightVTree$
        ))
      })

    Map("dom" -> vtree$)
  }

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  class SliderPropsDriver(props: Props) extends Driver[Unit] {

    def sliderProps: Observable[Props] = Observable.of(props)

  }

  def makeSliderPropsDriver(props: Props): SliderPropsDriver = new SliderPropsDriver(props)

}
