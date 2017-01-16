package com.campudus.scycle

import rxscalajs.Observable
import rxscalajs.subscription.AnonymousSubscription

class Driver[A] {

  def subscribe(inputs: Observable[A]): AnonymousSubscription = inputs.subscribe(_ => null)

}
