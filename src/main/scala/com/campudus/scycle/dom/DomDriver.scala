package com.campudus.scycle.dom

import com.campudus.scycle.Scycle.{DriverFunction, StreamAdapter}
import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rxscalajs._

class DomDriver extends DriverFunction[Hyperscript, Hyperscript] {

  private val selectedEvents: Subject[Hyperscript] = Subject[Hyperscript]()

  override def apply(
    stream: Observable[Hyperscript],
    adapter: StreamAdapter,
    driverName: String
  ): Observable[Hyperscript] = {
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

    Observable.create[Event](observer => {
      val obs = Observable
        .fromEvent(elem, eventName)
        .filter(ev => {
          val src = ev.srcElement
          val target = document.querySelector(what)
          console.log("hello", what, eventName, src.isSameNode(target))
          src.isSameNode(target)
        })

      obs.subscribe(observer): Unit
    })
  }

}
