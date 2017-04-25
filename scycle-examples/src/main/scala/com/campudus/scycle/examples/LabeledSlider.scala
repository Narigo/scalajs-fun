package com.campudus.scycle.examples

import com.campudus.scycle.Driver
import com.campudus.scycle.Scycle._
import com.campudus.scycle.dom.DomDriver.Dom
import com.campudus.scycle.dom.{DomDriver, _}
import com.campudus.scycle.examples.LabeledSlider.Props
import rxscalajs.Observable

object LabeledSlider {

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  class SliderPropsDriver(val props: Props) extends Driver[Unit]

  implicit val propsToDriver: SourcesMapper[Props.type, SliderPropsDriver] = new SourcesMapper

  def makeSliderPropsDriver(props: Props): SliderPropsDriver = new SliderPropsDriver(props)

  object SliderValue

  implicit val sliderValue: SinkMapper[SliderValue.type, Observable[Int]] = new SinkMapper

  def apply(sources: Sources): Sinks = {
    val sliderProps = sources(Props)
    val domDriver = sources(Dom)
    val slider = new LabeledSlider(sliderProps.props, domDriver)

    new SinksMap(Map(
      DomDriver.Dom -> slider.view,
      SliderValue -> slider.intent$
    ))
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
      newValue <- intent$
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
