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
    println(s"DomDriver.apply:stream.subscribe")
    stream.subscribe(hs => {
      println(s"stream.subscribe($hs)")
      val container = document.querySelector("#app")
      val diff = VirtualDom.diff(VirtualDom(container), hs)
      console.log("stream.subscribe:update(", container, ",", diff, ")")
      VirtualDom.update(container, diff)
    })

    println(s"DomDriver.apply:return $selectedEvents")
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
