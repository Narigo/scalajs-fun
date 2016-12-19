package com.campudus.scycle.dom

import com.campudus.scycle.Scycle.{DriverFunction, StreamAdapter}
import com.campudus.scycle.vdom.VirtualDom
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalajs.dom._
import rxscalajs._

class DomDriver extends DriverFunction[Hyperscript, Event] {

  private val selectedEvents: Subject[Event] = Subject()

  override def apply(
    stream: Observable[Hyperscript],
    adapter: StreamAdapter,
    driverName: String
  ): Observable[Event] = {
    println(s"DomDriver.apply:stream.subscribe = $stream")

    stream.subscribe(hs => {
      println(s"DomDriver.apply:stream.subscribe($hs)")
      val container = document.querySelector("#app")
      val diff = VirtualDom.diff(VirtualDom(container), hs)
      println(s"DomDriver.apply:stream.subscribe:update($container, $diff)")
      diff match {
        case List(Replacement(_, null)) => println("DomDriver.apply:stream.subscribe:diff was null")
        case diffs => VirtualDom.update(container, diffs)
      }
    })

    println(s"DomDriver.apply:return $selectedEvents")
    selectedEvents
  }

  def selectEvent(what: String, eventName: String): Observable[Event] = {
    println(s"DomDriver.selectEvent($what, $eventName) -> adding to selectedEvents")
    val elem = document.querySelector("#app")

    Observable
      .fromEvent(elem, eventName)
      .filter(ev => {
        println(s"DomDriver.selectEvent($what, $eventName):Observable.filter($ev)")
        val src = ev.srcElement
        val target = document.querySelector(what)
        src.isSameNode(target)
      })
      .map(ev => {
        println(s"DomDriver.selectEvent($what, $eventName):Observable.map($ev)")
        selectedEvents.next(ev)
        ev
      })
  }

}
