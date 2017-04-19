package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, Sources}
import com.campudus.scycle.dom.{DomDriver, _}
import com.campudus.scycle.examples.ScycleApp.{Props, SliderPropsDriver}
import org.scalajs.dom
import rxscalajs.Observable

object LabeledSlider {

  def apply(sources: Sources): Sinks = {
    val sliderProps = sources("props").asInstanceOf[SliderPropsDriver].sliderProps
    val domDriver = sources("dom").asInstanceOf[DomDriver]
    val slider = new LabeledSlider(sliderProps, domDriver)

    Map(
      "dom" -> slider.view
    )
  }
}

class LabeledSlider(props$: Observable[Props], domDriver: DomDriver) {

  dom.console.log("intent of LabeledSlider, domSelector=", domDriver.domSelector)
  private val intent$ = domDriver
    .select(".labeled-slider")
    .events("input")
    .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)

  dom.console.log("model of LabeledSlider using intent$=", intent$.toString)
  private val model$ = {
    props$.combineLatestWith(intent$) {
      (props, newValue) => {
        dom.console.log("in model, changing", props.value, "to", newValue)
        props.copy(value = newValue)
      }
    }
//    // FIXME props.value is always start value instead of older value -> combineLatest ?
//    for {
//      props <- props$
//      newValue <- intent$.startWith(props.value)
//    } yield {
//      dom.console.log("in model, changing", props.value, "to", newValue)
//      props.copy(value = newValue)
//    }
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
