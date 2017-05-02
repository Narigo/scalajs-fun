package com.campudus.scycle.examples

import com.campudus.scycle.Scycle._
import com.campudus.scycle._
import com.campudus.scycle.examples.LabeledSlider._
import com.campudus.scycle.dom.DomDriver._
import com.campudus.scycle.dom._
import com.campudus.scycle.http.HttpDriver._
import com.campudus.scycle.http.{Get, User}
import rxscalajs.Observable

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.util.Random

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    println("main export")

    Scycle.run(logic, drivers)
  }

  val drivers: DriversDefinition = new SourcesMap() +
    (Dom -> makeDomDriver("#app")) +
    (Http -> makeHttpDriver())

  def logic(sources: Sources): Sinks = {
    val weightSliderProps = Props("Weight", "kg", 40, 150, 70)
    val heightSliderProps = Props("Height", "cm", 140, 220, 170)

    val weightSources = new SourcesMap() +
      (Dom -> sources(Dom).select("#weight")) +
      (Props -> makeSliderPropsDriver(weightSliderProps))
    val heightSources = new SourcesMap() +
      (Dom -> sources(Dom).select("#height")) +
      (Props -> makeSliderPropsDriver(heightSliderProps))

    val WeightSlider = LabeledSlider(weightSources)
    val HeightSlider = LabeledSlider(heightSources)

    val weightVTree$ = WeightSlider(DomDriver.Dom)
    val heightVTree$ = HeightSlider(DomDriver.Dom)

    val weightValue$ = WeightSlider(LabeledSlider.SliderValue)
    val heightValue$ = HeightSlider(LabeledSlider.SliderValue)

    val bmi$ = Observable.combineLatest(List(weightValue$, heightValue$)).map({
      case weight :: height :: Nil =>
        val heightMeters = height * 0.01
        val bmi = Math.round(weight / (heightMeters * heightMeters))
        org.scalajs.dom.console.log(s"current bmi is $bmi")
        bmi
    })

    val request$ = sources(Http).request(Get(s"http://jsonplaceholder.typicode.com/users/${Random.nextInt(10) + 1}"))

    val user$ = request$.map(res => {
      if (res == null) {
        User("test", "test@test.de", "http://test.de")
      } else {
        val user = res.response
        User(user.username.toString, user.email.toString, user.website.toString)
      }
    })

    val vtree$: Observable[Hyperscript] = Observable.combineLatest(List(weightVTree$, heightVTree$, bmi$, user$)).map{
      case (weightVTree: Hyperscript) :: (heightVTree: Hyperscript) :: (bmi: Long) :: (user: User) :: Nil =>
        Div(id = "app", children = List(
          Div(id = "weight", children = List(weightVTree)),
          Div(id = "height", children = List(heightVTree)),
          Div(children = List(
            Text(s"BMI is $bmi")
          )),
          Div(children = List(
            Text(s"User is ${user.name} (${user.email}), ${user.website}")
          ))
        ))
    }

    new SinksMap() + (Dom -> vtree$)
  }

}
