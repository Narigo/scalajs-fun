package com.campudus.scycle.dom

import com.campudus.scycle.Scycle.{DriverFunction, StreamAdapter}
import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rxscalajs._

class DomDriver extends DriverFunction[Hyperscript, Event] {

  private val selectedEvents: Subject[Event] = Subject()

  override def apply(
    stream: Observable[Hyperscript],
    adapter: StreamAdapter,
    driverName: String
  ): Observable[Event] = {
    println(s"apply domdriver")

    stream.subscribe(
      hs => {
        val container = document.querySelector("#app")
        val diff = VirtualDom.diff(VirtualDom(container), hs)
        VirtualDom.update(container, diff)
      }
    )

    selectedEvents
  }

  def selectEvent(what: String, eventName: String): Observable[Event] = {
    val elem = document.querySelector("#app")
    console.log("searching for", what, eventName, elem)

    Observable
      .fromEvent(elem, eventName)
      .filter(ev => {
        val src = ev.srcElement
        val target = document.querySelector(what)
        console.log("hello", what, eventName, src.isSameNode(target))
        src.isSameNode(target)
      })
      .flatMap(ev => {
        selectedEvents.next(ev)
        selectedEvents
      })
  }

}
