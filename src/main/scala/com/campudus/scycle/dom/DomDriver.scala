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

  def selectEvent(what: String, eventName: String): Observable[Event] = {
    console.log("searching for", what, eventName)
    Observable
      .fromEvent(document.querySelector("#app"), eventName)
      .map({ ev =>
        console.log("got an event", ev)
        ev
      })
      .filter({ ev =>
        val src = ev.srcElement
        val target = document.querySelector(what)
        console.log("target=", target)
        console.log("src=", src)
        console.log("target == src =", src == target)
        src == target
      })
  }

}

object DomDriver extends Driver {

  def apply(input: Observable[_]): DomDriver = new DomDriver(input.asInstanceOf[Observable[Hyperscript]])

}
