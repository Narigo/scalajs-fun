package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, _}
import com.campudus.scycle._
import com.campudus.scycle.dom.DomDriver._
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

  val drivers: DriversDefinition = new DriverMap() + (DomDriverKey -> makeDomDriver("#app"))

  def logic(sources: Sources): Sinks = {
    val heightSliderProps = Props("Height", "cm", 140, 220, 170)
    val weightSliderProps = Props("Weight", "kg", 40, 150, 70)

    val WeightSlider = isolate(LabeledSlider.apply)("weight-slider")
    val HeightSlider = isolate(LabeledSlider.apply)("height-slider")

    val vtree$ = for {
      weightVTree <- WeightSlider(Map(
        DomDriverKey -> sources.get(DomDriverKey),
        SliderPropsKey -> makeSliderPropsDriver(weightSliderProps)))(DomDriverKey).asInstanceOf[Observable[Hyperscript]]
      heightVTree <- HeightSlider(Map(
        DomDriverKey -> sources.get(DomDriverKey),
        SliderPropsKey -> makeSliderPropsDriver(heightSliderProps)))(DomDriverKey).asInstanceOf[Observable[Hyperscript]]
    } yield {
      Div(id = "app", children = List(
        weightVTree,
        heightVTree
      ))
    }

    Map(DomDriverKey -> vtree$)
  }

  def isolate(apply: Sources => Sinks)(namespace: String): Sources => Sinks = {
    (sources: Sources) => {
      val isolatedSources: Sources = sources + ("dom" -> sources.get(DomDriverKey).select(s"#$namespace"))
      val sinks: Sinks = apply(isolatedSources)
      val newDomSink = for {
        hs <- sinks.get(DomDriverKey).asInstanceOf[Observable[Hyperscript]]
      } yield {
        Div(id = namespace, children = List(hs))
      }

      sinks + (DomDriverKey -> newDomSink)
    }
  }

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  class SliderPropsDriver(props: Props) extends Driver[Unit] {

    def sliderProps: Observable[Props] = Observable.of(props)

  }

  def makeSliderPropsDriver(props: Props): SliderPropsDriver = new SliderPropsDriver(props)

}
