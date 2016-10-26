package com.campudus.scycle.dom

import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rxscalajs._

class DomDriver(input: Observable[Hyperscript]) extends Observable[Hyperscript](input.inner) {

  println(s"apply domdriver")

  input.subscribe(
    { hs => {
      {
        val container = document.querySelector("#app")
        val diff = VirtualDom.diff(VirtualDom(container), hs)
        VirtualDom.update(container, diff)
      }
    }
    }
  )

  def selectEvent(what: String, eventName: String): Observable[Event] = {
    val elem = document.querySelector("#app")
    console.log("searching for", what, eventName, elem)

    Observable.create(
      observer => {
        val obs = Observable
          .fromEvent(elem, eventName)
          .filter(
            ev => {
              val src = ev.srcElement
              val target = document.querySelector(what)
              console.log("hello", what, eventName, src.isSameNode(target))
              src.isSameNode(target)
            }
          )

        obs.subscribe(observer)
      }
    )
  }

}

object DomDriver {

  def apply(input: Observable[_]): DomDriver = new DomDriver(input.asInstanceOf[Observable[Hyperscript]])

}
