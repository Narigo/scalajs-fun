package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rxscalajs._

class DomDriver(selector: String, input: Observable[Hyperscript]) extends Driver {

  println("initializing dom driver...")

  def selectEvents(selector: String, event: String): Observable[Event] = {
    document.addEventListener(event, (e: Event) => {
      console.log("got an event", e)
      if (e.srcElement == document.querySelector(selector)) {
        console.log("pushing it through!")
      } else {
      }
    })
    Observable(null)
  }

}

