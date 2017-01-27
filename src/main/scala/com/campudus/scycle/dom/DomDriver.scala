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
  private val selectedEvents: mutable.Map[(String, String), (Subject[Event], AnonymousSubscription)] = mutable.Map.empty

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
        case ((what, eventName), (subj, subs)) =>
          println(s"unsubscribe old event $eventName on $what into $subj with $subs")
          subs.unsubscribe()
          println(s"subscribe new event $eventName on $what into $subj")
          val subsNew = Observable.fromEvent(document.querySelector(what), eventName).subscribe(subj)
          println(s"subscribed to new event $eventName on $what into $subj with $subsNew")
          (what, eventName) -> (subj, subsNew)
      })
    })
  }

  def selectEvent(what: String, eventName: String): Observable[Event] = {
    val subj = Subject[Event]()
    val subs = Observable.fromEvent(document.querySelector(what), eventName).map(ev => {
      println("selected event happened")
      ev
    }).subscribe(subj.next(_))
    selectedEvents += (what -> eventName) -> (subj, subs)
    println(s"added event $eventName on $what into $subj")
    subj
  }

}

object DomDriver {

  def makeDomDriver(domSelector: String) = new DomDriver(domSelector)

}
