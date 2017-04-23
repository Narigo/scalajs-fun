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

    val weightValue$ = WeightSlider("value").asInstanceOf[Observable[Int]]
    val heightValue$ = HeightSlider("value").asInstanceOf[Observable[Int]]

    val bmi$ = Observable.combineLatest(List(weightValue$, heightValue$)).map({
      case weight :: height :: Nil =>
        val heightMeters = height * 0.01
        Math.round(weight / (heightMeters * heightMeters))
    })

    val vtree$ = Observable.combineLatest(List(weightVTree$, heightVTree$, bmi$)).map{
      case weightVTree :: heightVTree :: bmi :: Nil =>
        Div(id = "app", children = List(
          Div(id = "weight", children = List(weightVTree.asInstanceOf[Hyperscript])),
          Div(id = "height", children = List(heightVTree.asInstanceOf[Hyperscript])),
          Div(children = List(
            Text(s"BMI is $bmi")
          ))
        ))
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
        Div(id = namespace, children = List(hs))
      }

      sinks + ("dom" -> newDomSink)
    }
  }

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  class SliderPropsDriver(val props: Props) extends Driver[Unit]

  def makeSliderPropsDriver(props: Props): SliderPropsDriver = new SliderPropsDriver(props)

}
