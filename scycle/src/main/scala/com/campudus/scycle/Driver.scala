package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Subject}

trait Driver[A] {

  def createSubject(): Subject[A] = {
    org.scalajs.dom.console.log("in Driver.createSubject()")
    val s = Subject.apply[A]()
    org.scalajs.dom.console.log("after Driver.createSubject:Subject.apply()")
    s
  }

  def subscribe(inputs: Observable[A]): AnonymousSubscription = inputs.subscribe(_ => null)

}
