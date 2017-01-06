package com.campudus.scycle.dom

import com.campudus.scycle.Scycle.DriverFunction
import com.campudus.scycle.vdom.VirtualDom
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalajs.dom._
import rxscalajs._

class DomDriver extends DriverFunction {

  private val selectedEvents: Subject[Event] = Subject()

  override def apply[A, B](stream: Observable[A], driverName: String): Observable[B] = {
    myApply(stream.asInstanceOf[Observable[Hyperscript]], driverName).asInstanceOf[Observable[B]]
  }

  def myApply(
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
      .map(ev => {
        selectedEvents.next(ev)
        ev
      })
  }

}
