package com.campudus.scycle.examples

import com.campudus.scycle.Scycle._
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
    "dom" -> makeDomDriver("#app"),
    "props" -> makeSliderPropsDriver()
  )

  def logic(sources: Sources): Sinks = {
    val change$ = intent(sources("dom").asInstanceOf[DomDriver])
    val state$ = model(change$, sources("props").asInstanceOf[SliderPropsDriver].props)
    val vtree$ = view(state$)

    Map(
      "dom" -> vtree$
    )
  }

  def intent(domDriver: DomDriver): Observable[Int] = {
    val change$ = domDriver
      .selectEvent(".slider", "input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)

    change$
  }

  def model(change$: Observable[Int], props$: Observable[Props]): Observable[Props] = {
    change$.startWith(70).combineLatest(props$).map(tuple => {
      val (newValue, props) = tuple
      props.copy(value = newValue)
    })
  }

  def view(value$: Observable[Props]): Observable[Hyperscript] = {
    value$.map(props => {
      Div(id = "app", className = "labeled-slider", children = List(
        Label(children = List(Text(s"${props.label}: ${props.value} ${props.unit}"))),
        Input(className = "slider", options = List(
          "type" -> "range", "min" -> s"${props.min}", "max" -> s"${props.max}", "value" -> s"${props.value}"
        ))
      ))
    })
  }

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  class SliderPropsDriver extends Driver[Unit] {

    def props: Observable[Props] = Observable.of(Props("Weight", "kg", 40, 170, 70))

  }

  def makeSliderPropsDriver(): SliderPropsDriver = new SliderPropsDriver
}
