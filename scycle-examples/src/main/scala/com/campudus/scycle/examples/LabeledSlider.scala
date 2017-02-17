package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, Sources}
import com.campudus.scycle.dom._
import com.campudus.scycle.examples.ScycleApp.{Props, SliderPropsDriver}
import rxscalajs.Observable

object LabeledSlider {

  def apply(sources: Sources): Sinks = {
    val props$ = sources("props").asInstanceOf[SliderPropsDriver].sliderProps
    val change$ = intent(sources("dom").asInstanceOf[DomDriver], props$)
    val state$ = model(change$, props$)
    val vtree$ = view(state$)

    Map(
      "dom" -> vtree$
    )
  }

  def intent(domDriver: DomDriver, props$: Observable[Props]): Observable[Int] = {
    val change$ = props$.flatMap(props => {
      domDriver
        .select(s"#${props.id}")
        .events("input")
        .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)
    })

    change$
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
      Div(id = props.id, className = "labeled-slider", children = List(
        Label(children = List(Text(s"${props.label}: ${props.value} ${props.unit}"))),
        Input(className = "slider", options = List(
          "type" -> "range", "min" -> s"${props.min}", "max" -> s"${props.max}", "value" -> s"${props.value}"
        ))
      ))
    })
  }

}
