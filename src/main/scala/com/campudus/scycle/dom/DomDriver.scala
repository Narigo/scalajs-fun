package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalajs.dom._
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

import scala.collection.mutable

class DomDriver private(domSelector: String) extends Driver[Hyperscript] {

  import org.scalajs.dom.console._

  // FIXME maybe use a subject and feed it new values whenever dom changes?
  private val selectedEvents: mutable.Map[(String, String), (Subject[Event], AnonymousSubscription)] = mutable.Map.empty

  override def subscribe(inputs: Observable[Hyperscript]): AnonymousSubscription = {
    inputs.subscribe(hs => {
      // FIXME how to get rid of this null check caused by Scycle -> sinkProxies(name).next(null)
      if (hs != null) {
        val container = document.querySelector(domSelector)
        if (container == null) {
          log("NO CONTAINER FOUND WITH", domSelector)
        }
        val diff = VirtualDom.diff(VirtualDom(container), hs)
        diff match {
          case List(Replacement(_, null)) =>
          case diffs => VirtualDom.update(container, diffs)
        }

        selectedEvents.foreach({
          case ((what, eventName), (subj, subs)) =>
            log(s"unsubscribe old event $eventName on $what into $subj with subscription ${subs.hashCode()}")
            subs.unsubscribe()
            log(s"subscribe new event $eventName on $what into $subj")
            val subsNew = Observable.fromEvent(document.querySelector(what), eventName).subscribe(subj)
            log(s"subscribed to new event $eventName on $what into $subj with subscriptions ${subsNew.hashCode()}")
            (what, eventName) -> (subj, subsNew)
        })
      }
    })
  }

  def selectEvent(what: String, eventName: String): Observable[Event] = {
    val subj = Subject[Event]()
    val subs = Observable.fromEvent(document.querySelector(what), eventName).map(ev => {
      log("selected event happened")
      ev
    }).subscribe(subj)
    selectedEvents += (what -> eventName) -> (subj, subs)
    log(s"added event $eventName on $what into $subj with subscription ${subs.hashCode()}")
    subj
  }

}

object DomDriver {

  def makeDomDriver(domSelector: String) = new DomDriver(domSelector)

}
