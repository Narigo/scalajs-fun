package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.dom.DomDriver.SelectedEvents
import com.campudus.scycle.vdom.VirtualDom
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalajs.dom._
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

import scala.collection.mutable

class DomDriver private(domSelector: String, selectedEvents: SelectedEvents = mutable.Map.empty)
  extends Driver[Hyperscript] {

  override def subscribe(inputs: Observable[Hyperscript]): AnonymousSubscription = {
    inputs.subscribe(hs => {
      // FIXME how to get rid of this null check caused by Scycle -> sinkProxies(name).next(null)
      if (hs != null) {
        val container = document.querySelector(domSelector)
        if (container == null) {
          console.log("NO CONTAINER FOUND WITH", domSelector)
        }
        val diff = VirtualDom.diff(VirtualDom(container), hs)
        diff match {
          case List(Replacement(_, null)) =>
          case diffs => VirtualDom.update(container, diffs)
        }

        selectedEvents.foreach({
          case ((what, eventName), (subj, subs)) =>
            subs.unsubscribe()
            subs.unsubscribe()
            val subsNew = Observable.fromEvent(document.querySelector(what), eventName).subscribe(subj)
            org.scalajs.dom.console.log("subscribing to", what, eventName, "into", subj.toString, "again")
            selectedEvents.remove(what, eventName)
            org.scalajs.dom.console.log("selectedEvents got", selectedEvents.size)
            selectedEvents += (what, eventName) -> (subj, subsNew)
        })
      }
    })
  }

  def select(what: String): DomDriver = {
    new DomDriver(s"$domSelector $what", DomDriver.this.selectedEvents)
  }

  def events(what: String): Observable[Event] = {
    val subj = Subject[Event]()
    val selected = document.querySelector(domSelector)
    console.log("selecting", domSelector, selected)
    if (selected != null) {
      val subs = Observable.fromEvent(selected, what).subscribe(subj)
      org.scalajs.dom.console.log("subscribing to", domSelector, what, subs)
      selectedEvents += (domSelector -> what) -> (subj, subs)
      subj
    } else {
      Observable.just()
    }
  }

}

object DomDriver {

  def makeDomDriver(domSelector: String) = new DomDriver(domSelector)

  type SelectedEvents = mutable.Map[(String, String), (Subject[Event], AnonymousSubscription)]

}
