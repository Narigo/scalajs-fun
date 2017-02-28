package com.campudus.scycle.examples

import com.campudus.scycle.Scycle.{Sinks, Sources}
import com.campudus.scycle.dom.DomDriver.DomDriverKey
import com.campudus.scycle.dom._
import com.campudus.scycle.{Driver, DriverKey, DriverType}
import rxscalajs.Observable

object LabeledSlider {

  case class Props(label: String, unit: String, min: Int, max: Int, value: Int)

  object SliderPropsDriverKey extends DriverKey

  implicit val SliderPropsDriverType = new DriverType[SliderPropsDriverKey.type, SliderPropsDriver]

  class SliderPropsDriver(props: Props) extends Driver[Unit] {

    def sliderProps: Observable[Props] = Observable.of(props)

  }

  def makeSliderPropsDriver(props: Props): SliderPropsDriver = new SliderPropsDriver(props)

  def apply(sources: Sources): Sinks = {
    val props$ = sources.get(SliderPropsDriverKey).get.sliderProps
    val change$ = intent(sources.get(DomDriverKey).get)
    val state$ = model(change$, props$)
    val vtree$ = view(state$)

    Map(
      DomDriverKey -> vtree$
    )
  }

  def intent(domDriver: DomDriver): Observable[Int] = {
    domDriver
      .select(".labeled-slider")
      .events("input")
      .map(_.target.asInstanceOf[org.scalajs.dom.html.Input].value.toInt)
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
