package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, _}
import com.campudus.scycle._
import com.campudus.scycle.dom.DomDriver._
import com.campudus.scycle.dom._
import com.campudus.scycle.examples.LabeledSlider._
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
    println("test1")

    val WeightSlider = isolate(LabeledSlider.apply)("weight-slider")
    val HeightSlider = isolate(LabeledSlider.apply)("height-slider")
    println("test2")

    val vtree$ = for {
      weightVTree <- WeightSlider(new DriverMap() +
        (DomDriverKey -> sources.get(DomDriverKey).get) +
        (SliderPropsDriverKey -> makeSliderPropsDriver(weightSliderProps))
      )(DomDriverKey)
      _ = println("test3")
      heightVTree <- HeightSlider(new DriverMap() +
        (DomDriverKey -> sources.get(DomDriverKey).get) +
        (SliderPropsDriverKey -> makeSliderPropsDriver(heightSliderProps))
      )(DomDriverKey)
    } yield {
      Div(id = "app", children = List(
        weightVTree.asInstanceOf[Hyperscript],
        heightVTree.asInstanceOf[Hyperscript]
      ))
    }

    Map(DomDriverKey -> vtree$)
  }

  def isolate(apply: Sources => Sinks)(namespace: String): Sources => Sinks = {
    (sources: Sources) => {
      val isolatedSources: Sources = sources + (DomDriverKey -> sources.get(DomDriverKey).get.select(s"#$namespace"))
      val sinks: Sinks = apply(isolatedSources)
      println("App.isolate")
      val newDomSink = for {
        hs <- sinks(DomDriverKey).asInstanceOf[Observable[Hyperscript]]
      } yield {
        Div(id = namespace, children = List(hs))
      }

      println("App.isolate returning")
      sinks + (DomDriverKey -> newDomSink)
    }
  }

}
