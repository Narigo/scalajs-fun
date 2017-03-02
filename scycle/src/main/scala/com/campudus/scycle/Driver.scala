package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Subject}

trait Driver[A] {

  def createSubject(): Subject[A] = Subject()

  def subscribe(inputs: Observable[A]): AnonymousSubscription = inputs.subscribe(_ => null)

}

class DriverKey

class DriverType[K <: DriverKey, V <: Driver[_]]

class DriverMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[K <: DriverKey, V <: Driver[_]](kv: (K, V))
    (implicit ev: DriverType[K, V]): DriverMap = {
    new DriverMap(underlying + kv)
  }

  def -[K](k: K): DriverMap = new DriverMap(underlying - k)

  def get[K <: DriverKey, V <: Driver[_]](k: K)(implicit ev: DriverType[K, V]): Option[V] = {
    println(s"test DriverMap $k")
    underlying.get(k).asInstanceOf[Option[V]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys[K <: DriverKey](implicit ev: DriverType[K, _]): Iterable[K] = {
    underlying.keys.map(k => k.asInstanceOf[K])
  }

}
