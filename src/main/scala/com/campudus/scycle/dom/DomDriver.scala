package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalajs.dom._
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

import scala.collection.mutable

class DomDriver private(domSelector: String) extends Driver[Hyperscript] {

  // FIXME maybe use a subject and feed it new values whenever dom changes?
  private val selectedEvents: mutable.Map[(String, String), Observable[Event]] = mutable.Map.empty

  override def subscribe(inputs: Observable[Hyperscript]): AnonymousSubscription = {
    inputs.subscribe(hs => {
      val container = document.querySelector(domSelector)
      val diff = VirtualDom.diff(VirtualDom(container), hs)
      diff match {
        case List(Replacement(_, null)) =>
        case diffs => VirtualDom.update(container, diffs)
      }

      println("updated dom")

      selectedEvents.foreach({
        case ((what, eventName), event$) =>
          println(s"foreach selectedEvents $what -> $eventName -> ${event$}")
          event$.concat(Observable.fromEvent(document.querySelector(what), eventName))
      })
    })
  }

  def selectEvent(what: String, eventName: String): Observable[Event] = {
    val event$ = Observable.fromEvent(document.querySelector(what), eventName).map(ev => {
      println("selected event happened")
      ev
    })
    selectedEvents += (what -> eventName) -> event$
    event$
  }

}

object DomDriver {

  def makeDomDriver(domSelector: String) = new DomDriver(domSelector)

}
