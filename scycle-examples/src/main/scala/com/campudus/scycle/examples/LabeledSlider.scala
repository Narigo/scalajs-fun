package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, Sources}
import com.campudus.scycle.dom.DomDriver
import com.campudus.scycle.dom._
import com.campudus.scycle.examples.ScycleApp.{Props, SliderPropsDriver}
import org.scalajs.dom
import rxscalajs.Observable

object LabeledSlider {

  def apply(sources: Sources): Sinks = {
    val props$ = sources("props").asInstanceOf[SliderPropsDriver].sliderProps
    val change$ = intent(sources("dom").asInstanceOf[DomDriver])
    val state$ = model(change$, props$)
    val vtree$ = view(state$)

    Map(
      "dom" -> vtree$
    )
  }

  def intent(domDriver: DomDriver): Observable[Int] = {
    dom.console.log(s"domdriver=$domDriver")
    domDriver
      .select(".labeled-slider")
      .events("input")
      .map(event => {
        dom.console.log("test event", event)
        event.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt
      })
  }

  def model(change$: Observable[Int], props$: Observable[Props]): Observable[Props] = {
    for {
      props <- props$
      newValue <- change$.startWith(props.value)
    } yield {
      props.copy(value = newValue)
    }
  }

  def view(value$: Observable[Props]): Observable[Hyperscript] = {
    value$.map(props => {
      Div(className = "labeled-slider", children = List(
        Label(children = List(Text(s"${props.label}: ${props.value} ${props.unit}"))),
        Input(className = "slider", options = List(
          "type" -> "range", "min" -> s"${props.min}", "max" -> s"${props.max}", "value" -> s"${props.value}"
        ))
      ))
    })
  }

}
