package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.dom.DomDriver.SelectedEvents
import com.campudus.scycle.vdom.VirtualDom
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalajs.dom._
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

import scala.collection.mutable

class DomDriver private(val domSelector: String, selectedEvents: SelectedEvents = mutable.Map.empty)
  extends Driver[Hyperscript] {

  override def subscribe(inputs: Observable[Hyperscript]): AnonymousSubscription = {
    inputs.subscribe(hs => {
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
          val subsNew = Observable.fromEvent(document.querySelector(what), eventName).subscribe(subj)
          selectedEvents += (what, eventName) -> (subj, subsNew)
      })
    })
  }

  def select(what: String): DomDriver = {
    new DomDriver(s"$domSelector $what", DomDriver.this.selectedEvents)
  }

  def events(what: String): Observable[Event] = {
    val subj = Subject[Event]()
    val selected = document.querySelector(domSelector)
    val subs = if (selected != null) {
      Observable.fromEvent(selected, what).subscribe(subj)
    } else {
      Observable.fromEvent(document.querySelector("#app"), what).subscribe(subj)
    }
    selectedEvents += (domSelector -> what) -> (subj, subs)
    subj
  }

}

object DomDriver {

  def makeDomDriver(domSelector: String) = new DomDriver(domSelector)

  type SelectedEvents = mutable.Map[(String, String), (Subject[Event], AnonymousSubscription)]

}
