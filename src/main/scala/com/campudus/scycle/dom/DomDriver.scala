package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rxscalajs._

class DomDriver(input: Observable[Hyperscript]) extends Driver {

  println(s"apply domdriver")

  input.subscribe({ hs =>
    val container = document.querySelector("#app")
    val diff = VirtualDom.diff(VirtualDom(container), hs)
    VirtualDom.update(container, diff)
  })

  def selectEvent(what: String, name: String): Observable[Event] = {
    Observable
      .fromEvent(document.querySelector("#app"), name)
      .filter(ev => ev.srcElement.isSameNode(document.querySelector(what)))
  }

}

object DomDriver extends Driver {

  def apply(input: Observable[_]): DomDriver = new DomDriver(input.asInstanceOf[Observable[Hyperscript]])

}
