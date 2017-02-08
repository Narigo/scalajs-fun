package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Subject}

class Driver[A] {

  def createSubject(): Subject[A] = Subject()

  def subscribe(inputs: Observable[A]): AnonymousSubscription = inputs.subscribe(_ => null)

}
