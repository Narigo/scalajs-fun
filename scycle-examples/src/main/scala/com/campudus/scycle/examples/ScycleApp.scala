package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, _}
import com.campudus.scycle._
import com.campudus.scycle.dom.DomDriver.makeDomDriver
import com.campudus.scycle.dom._
import org.scalajs.dom
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
    val weightSliderProps = Props("Weight", "kg", 40, 150, 70)
    val heightSliderProps = Props("Height", "cm", 140, 220, 170)

    val weightSources = Map(
      "dom" -> sources("dom").asInstanceOf[DomDriver].select("#weight"),
      "props" -> makeSliderPropsDriver(weightSliderProps)
    )
    val heightSources = Map(
      "dom" -> sources("dom").asInstanceOf[DomDriver].select("#height"),
      "props" -> makeSliderPropsDriver(heightSliderProps)
    )

    val WeightSlider = LabeledSlider(weightSources)
    val HeightSlider = LabeledSlider(heightSources)

    val weightVTree$ = WeightSlider("dom").asInstanceOf[Observable[Hyperscript]]
    val heightVTree$ = HeightSlider("dom").asInstanceOf[Observable[Hyperscript]]

    val vtree$ = weightVTree$.combineLatestWith(heightVTree$){
      (weightVTree, heightVTree) => {
        dom.console.log("weightVTree", weightVTree.toString)
        dom.console.log("heightVTree", heightVTree.toString)
        Div(id = "app", children = List(
          Div(id = "weight", children = List(weightVTree)),
          Div(id = "height", children = List(heightVTree))
        ))
      }
    }

    Map("dom" -> vtree$)
  }

  private def isolate(apply: Sources => Sinks)(namespace: String): Sources => Sinks = {
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
