package com.campudus.scycle.dom

import org.scalajs.dom._
import rxscalajs._

object DomDriver {
  def apply(input: Observable[Hyperscript]): Observable[Event] = {
    Observable.fromEvent(document.querySelector("#app"), "click")
  }
}
