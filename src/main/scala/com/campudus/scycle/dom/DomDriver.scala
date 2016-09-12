package com.campudus.scycle.dom

import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rxscalajs._

object DomDriver {
  def apply(input: Observable[Hyperscript]): Observable[Event] = {
    println("apply domdriver")

    input.subscribe({ hs =>
      val container = document.querySelector("#app")
      val diff = VirtualDom.diff(VirtualDom(container), hs)
      VirtualDom.update(container, diff)
    })

    Observable.fromEvent(document.querySelector("#app"), "click")
  }
}
