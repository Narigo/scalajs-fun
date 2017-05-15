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

    val user$ = response$$.flatMap(res$ => {
      org.scalajs.dom.console.log("######### A response to map to user", res$.request.url)
      val newRes$ = res$.map(res => {
        org.scalajs.dom.console.log("######## The response = ", res.response)
        if (res == null) {
          org.scalajs.dom.console.log("result in test user")
          User("test", "test@test.de", "http://test.de")
        } else {
          val user = res.response.response
          org.scalajs.dom.console.log("result in correct user", user)
          User(user.username.toString, user.email.toString, user.website.toString)
        }
      })
      org.scalajs.dom.console.log("######### after map")
      newRes$.subscribe(sth => {
        org.scalajs.dom.console.log("######### subscribe in res", sth.toString)
      })
      org.scalajs.dom.console.log("######### before returning the result")
      newRes$
    }).startWith(null)

    user$.subscribe(user => {
      org.scalajs.dom.console.log("user subscription yields", Option(user).toString)
    })

    val vtree$: Observable[Hyperscript] = Observable.combineLatest(List(weightVTree$, heightVTree$, bmi$/*, user$*/))
      .map({
        case (weightVTree: Hyperscript) :: (heightVTree: Hyperscript) :: (bmi: Long) /*:: (user: Any)*/ :: Nil =>
          /*        org.scalajs.dom.console.log(s"A vtree user!", user.toString)
                  val userText: Text = if (user == null) {
                    org.scalajs.dom.console.log("user is null")
                    Text(s"No user")
                  } else {
                    org.scalajs.dom.console.log("user is not null")
                    Text(s"User is ${user.name} (${user.email}), ${user.website}")
                  }
          */ val userText = Text("No user")

          org.scalajs.dom.console.log("some stuff")
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
