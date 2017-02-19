package com.campudus.scycle.examples

import com.campudus.scycle.Scycle._
import com.campudus.scycle._
import com.campudus.scycle.dom.DomDriver.{DomSource, makeDomDriver}
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
    val heightSources = sources("dom").asInstanceOf[Observable[DomSource]]
    val weightSources = sources("dom").asInstanceOf[Observable[DomSource]]
    val heightSinks = LabeledSlider(Map("dom" -> heightSources, "props" -> makeSliderPropsDriver(heightSliderProps)))
    val weightSinks = LabeledSlider(Map("dom" -> weightSources, "props" -> makeSliderPropsDriver(weightSliderProps)))

    val vtree$ = for {
      weightVTree <- weightSinks("dom").asInstanceOf[Observable[Hyperscript]]
      heightVTree <- heightSinks("dom").asInstanceOf[Observable[Hyperscript]]
    } yield {
      Div(id = "app", children = List(
        Div(id = "weight-slider", children = List(weightVTree)),
        Div(id = "height-slider", children = List(heightVTree))
      ))
    }

    Map("dom" -> vtree$)
  }

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  class SliderPropsDriver(props: Props) extends Driver[Unit] {

    def sliderProps: Observable[Props] = Observable.of(props)

  }

  def makeSliderPropsDriver(props: Props): SliderPropsDriver = new SliderPropsDriver(props)

}
