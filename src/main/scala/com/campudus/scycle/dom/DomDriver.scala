package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalajs.dom._
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

class DomDriver private (domSelector: String) extends Driver[Hyperscript] {

  override def subscribe(inputs: Observable[Hyperscript]): AnonymousSubscription = {
    inputs.subscribe(hs => {
      val container = document.querySelector(domSelector)
      val diff = VirtualDom.diff(VirtualDom(container), hs)
      diff match {
        case List(Replacement(_, null)) =>
        case diffs => VirtualDom.update(container, diffs)
      }
    })
  }

  def selectEvent(what: String, eventName: String): Observable[Event] = {
    val elem = document.querySelector("#app")

    Observable
      .fromEvent(elem, eventName)
      .filter(ev => {
        val src = ev.srcElement
        val target = document.querySelector(what)
        src.isSameNode(target)
      })
  }

}

object DomDriver {

  def makeDomDriver(domSelector: String) = new DomDriver(domSelector)

}
