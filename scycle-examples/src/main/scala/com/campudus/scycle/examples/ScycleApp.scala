package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, _}
import com.campudus.scycle._
import com.campudus.scycle.examples.LabeledSlider._
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

  val drivers: DriversDefinition = Map[Any, Driver[_]](
    Dom -> makeDomDriver("#app")
  )

  def logic(sources: Sources): Sinks = {
    org.scalajs.dom.console.log("hello in app")
    val weightSliderProps = Props("Weight", "kg", 40, 150, 70)
    val heightSliderProps = Props("Height", "cm", 140, 220, 170)

    val weightSources = Map[Any, Driver[_]](
      Dom -> sources(Dom).asInstanceOf[DomDriver].select("#weight"),
      "props" -> makeSliderPropsDriver(weightSliderProps)
    )
    val heightSources = Map[Any, Driver[_]](
      Dom -> sources(Dom).asInstanceOf[DomDriver].select("#height"),
      "props" -> makeSliderPropsDriver(heightSliderProps)
    )

    org.scalajs.dom.console.log("before labeledslider")
    val WeightSlider = LabeledSlider(weightSources)
    val HeightSlider = LabeledSlider(heightSources)
    org.scalajs.dom.console.log("after labeledslider")

    val weightVTree$ = WeightSlider(DomDriver.Dom)
    val heightVTree$ = HeightSlider(DomDriver.Dom)
    org.scalajs.dom.console.log("after weightslider/heightslider dom get")

    val weightValue$ = WeightSlider(LabeledSlider.SliderValue)
    val heightValue$ = HeightSlider(LabeledSlider.SliderValue)
    org.scalajs.dom.console.log("after weightslider/heightslider value get")

    val bmi$ = Observable.combineLatest(List(weightValue$, heightValue$)).map({
      case weight :: height :: Nil =>
        val heightMeters = height * 0.01
        val bmi = Math.round(weight / (heightMeters * heightMeters))
        org.scalajs.dom.console.log(s"current bmi is $bmi")
        bmi
    })
    org.scalajs.dom.console.log("after bmi calculation")

    val vtree$: Observable[Hyperscript] = Observable.combineLatest(List(weightVTree$, heightVTree$, bmi$)).map{
      case weightVTree :: heightVTree :: bmi :: Nil =>
        Div(id = "app", children = List(
          Div(id = "weight", children = List(weightVTree.asInstanceOf[Hyperscript])),
          Div(id = "height", children = List(heightVTree.asInstanceOf[Hyperscript])),
          Div(children = List(
            Text(s"BMI is $bmi")
          ))
        ))
    }

    org.scalajs.dom.console.log("resulting in sinkMap")
    new SinksMap() + (Dom -> vtree$)
  }

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  class SliderPropsDriver(val props: Props) extends Driver[Unit]

  def makeSliderPropsDriver(props: Props): SliderPropsDriver = new SliderPropsDriver(props)

}
