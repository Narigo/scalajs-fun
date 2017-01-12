package com.campudus.scycle.dom

import com.campudus.scycle.Scycle.{DriverFunction, side}
import com.campudus.scycle.vdom.VirtualDom
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalajs.dom._
import rxscalajs._

class DomDriver extends DriverFunction[Hyperscript, Event] {

  private val selectedEvents: Subject[Event] = Subject()

  override def apply(
    stream: Observable[Hyperscript],
    driverName: String
  ): Observable[Event] = {

    stream.subscribe(hs => {
      val container = document.querySelector("#app")
      val diff = VirtualDom.diff(VirtualDom(container), hs)
      diff match {
        case List(Replacement(_, null)) =>
        case diffs => VirtualDom.update(container, diffs)
      }
    })

    selectedEvents
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
      .map(side(selectedEvents.next))
  }

}
