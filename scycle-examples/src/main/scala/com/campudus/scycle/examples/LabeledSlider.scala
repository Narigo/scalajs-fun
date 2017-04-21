package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, Sources}
import com.campudus.scycle.dom.{DomDriver, _}
import com.campudus.scycle.examples.ScycleApp.{Props, SliderPropsDriver}
import org.scalajs.dom
import rxscalajs.{Observable, Subject}

object LabeledSlider {

  def apply(sources: Sources): Sinks = {
    val sliderProps = sources("props").asInstanceOf[SliderPropsDriver]
    val domDriver = sources("dom").asInstanceOf[DomDriver]
    val slider = new LabeledSlider(sliderProps.props, domDriver)

    Map(
      "dom" -> slider.view
    )
  }
}

class LabeledSlider(props: Props, domDriver: DomDriver) {

  dom.console.log("intent of LabeledSlider, domSelector=", domDriver.domSelector)

  private val intent$: Observable[Int] = {
    domDriver
      .select(".labeled-slider")
      .events("input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)
      .startWith(props.value)
  }

  dom.console.log("model of LabeledSlider using intent$=", intent$.toString)
  private val model$: Observable[Props] = {
    for {
      newValue <- intent$.startWith(props.value)
    } yield {
      dom.console.log("in model, changing to", newValue)
      props.copy(value = newValue)
    }
  }

  val view: Observable[Hyperscript] = model$.map(props => {
    dom.console.log("values changed", props.toString)
    Div(className = "labeled-slider", children = List(
      Label(children = List(Text(s"${props.label}: ${props.value} ${props.unit}"))),
      Input(className = "slider", options = List(
        "type" -> "range", "min" -> s"${props.min}", "max" -> s"${props.max}", "value" -> s"${props.value}"
      ))
    ))
  })

}
