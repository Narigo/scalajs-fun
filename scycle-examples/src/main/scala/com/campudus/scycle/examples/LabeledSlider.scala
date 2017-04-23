package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, Sources}
import com.campudus.scycle.dom.{DomDriver, _}
import com.campudus.scycle.examples.ScycleApp.{Props, SliderPropsDriver}
import rxscalajs.Observable

object LabeledSlider {

  def apply(sources: Sources): Sinks = {
    val sliderProps = sources("props").asInstanceOf[SliderPropsDriver]
    val domDriver = sources("dom").asInstanceOf[DomDriver]
    val slider = new LabeledSlider(sliderProps.props, domDriver)

    Map(
      "dom" -> slider.view,
      "value" -> slider.intent$
    )
  }
}

class LabeledSlider(props: Props, domDriver: DomDriver) {

  private val intent$: Observable[Int] = {
    domDriver
      .select(".labeled-slider")
      .events("input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)
      .startWith(props.value)
  }

  private val model$: Observable[Props] = {
    for {
      newValue <- intent$.startWith(props.value)
    } yield {
      props.copy(value = newValue)
    }
  }

  val view: Observable[Hyperscript] = model$.map(props => {
    Div(className = "labeled-slider", children = List(
      Label(children = List(Text(s"${props.label}: ${props.value} ${props.unit}"))),
      Input(className = "slider", options = List(
        "type" -> "range", "min" -> s"${props.min}", "max" -> s"${props.max}", "value" -> s"${props.value}"
      ))
    ))
  })

}
