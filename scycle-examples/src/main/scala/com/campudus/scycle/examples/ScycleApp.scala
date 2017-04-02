package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, _}
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

    val WeightSlider = isolate(LabeledSlider.apply)("weight-slider")
    val HeightSlider = isolate(LabeledSlider.apply)("height-slider")

    val vtree$ = for {
      weightVTree <- WeightSlider(Map("dom" -> sources("dom"), "props" -> makeSliderPropsDriver(weightSliderProps)))(
        "dom").asInstanceOf[Observable[Hyperscript]]
      heightVTree <- HeightSlider(Map("dom" -> sources("dom"), "props" -> makeSliderPropsDriver(heightSliderProps)))(
        "dom").asInstanceOf[Observable[Hyperscript]]
    } yield {
      Div(id = "app", children = List(
        weightVTree,
        heightVTree
      ))
    }

    Map("dom" -> vtree$)
  }

  def isolate(apply: Sources => Sinks)(namespace: String): Sources => Sinks = {
    (sources: Sources) => {
      val isolatedSources: Sources = sources + ("dom" -> sources("dom").asInstanceOf[DomDriver].select(s"#$namespace"))
      val sinks: Sinks = apply(isolatedSources)
      val newDomSink = for {
        hs <- sinks("dom").asInstanceOf[Observable[Hyperscript]]
      } yield {
        org.scalajs.dom.console.log("isolate", namespace, hs.toString())
        Div(id = namespace, children = List(hs))
      }

      sinks + ("dom" -> newDomSink)
    }
  }

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  class SliderPropsDriver(props: Props) extends Driver[Unit] {

    def sliderProps: Observable[Props] = Observable.of(props)

  }

  def makeSliderPropsDriver(props: Props): SliderPropsDriver = new SliderPropsDriver(props)

}
