package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rxscalajs._

class DomDriver(input: Observable[Hyperscript]) extends Driver {

  println("initializing dom driver...")

  def selectEvents(selector: String, event: String): Observable[Event] = {
    Observable.fromEvent(document.querySelector(selector), event)
  }

}

object DomDriver {
  def apply(input: Observable[Hyperscript]): DomDriver = new DomDriver(input)
}
