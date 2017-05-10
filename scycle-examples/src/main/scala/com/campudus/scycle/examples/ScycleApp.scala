package com.campudus.scycle.examples

import com.campudus.scycle.Scycle._
import com.campudus.scycle._
import com.campudus.scycle.examples.LabeledSlider._
import com.campudus.scycle.dom.DomDriver._
import com.campudus.scycle.dom._
import com.campudus.scycle.http.HttpDriver._
import com.campudus.scycle.http.{Get, Request, Response, User}
import rxscalajs.Observable

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

    val clicks$ = sources(Dom).events("click")

    val response$$ = sources(Http)
      .filter(response$ => {
        val result = response$.request.id == "user"
        org.scalajs.dom.console.log("Filter response$ results in url=", response$.request.url, "and result:", result)
        result
      })
      .map(x => {
        org.scalajs.dom.console.log("test??", x.request.url)
        x
      })

    response$$.subscribe(_ => {
      org.scalajs.dom.console.log("subscribe in app")
    })

    val user$ = response$$.map(res$ => {
      org.scalajs.dom.console.log("A response to map to user", res$.request.url)
      res$.map(res => {
        if (res == null) {
          User("test", "test@test.de", "http://test.de")
        } else {
          val user = res.response
          User(user.username.toString, user.email.toString, user.website.toString)
        }
      })
    }).startWith(null)

    val vtree$: Observable[Hyperscript] = Observable.combineLatest(List(weightVTree$, heightVTree$, bmi$, user$)).map({
      case (weightVTree: Hyperscript) :: (heightVTree: Hyperscript) :: (bmi: Long) :: (user: User) :: Nil =>
        val userText: Text = if (user == null) {
          Text(s"No user")
        } else {
          Text(s"User is ${user.name} (${user.email}), ${user.website}")
        }

        Div(id = "app", children = List(
          Div(id = "weight", children = List(weightVTree)),
          Div(id = "height", children = List(heightVTree)),
          Div(children = List(
            Text(s"BMI is $bmi")
          )),
          Div(children = List(
            Button(id = "request-user", children = List(Text(s"Request user"))),
            userText
          ))
        ))
    })

    val userRequest$ = clicks$.map(_ => {
      val randomInt = Random.nextInt(10) + 1
      org.scalajs.dom.console.log("clicked button, requesting new user", randomInt)
      Get("user", s"http://jsonplaceholder.typicode.com/users/$randomInt").asInstanceOf[Request]
    })

    new SinksMap() +
      (Dom -> vtree$) +
      (Http -> userRequest$)
  }

}
