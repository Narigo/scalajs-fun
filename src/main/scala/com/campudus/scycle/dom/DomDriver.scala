package com.campudus.scycle.dom

import org.scalajs.dom._
import rxscalajs._

object DomDriver {
  def apply(input: Observable[Hyperscript]): Observable[Event] = {
    println("apply domdriver")
    input
      .map { hs =>
        console.log(s"got a new input: $hs")
        hs
      }

    Observable
      .fromEvent(document.querySelector("#app"), "click")
      .map { ev =>
        console.log("clicked", ev)
        ev
      }
  }
}
