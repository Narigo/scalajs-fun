package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Subject}

trait Driver[A] {

  def createSubject(): Subject[A] = Subject()

  def subscribe(inputs: Observable[A]): AnonymousSubscription = inputs.subscribe(_ => null)

}

trait DriverKey

class DriverType[K <: DriverKey, V <: Driver[_]]

class DriverMap(underlying: Map[Any, Any] = Map.empty) {

  def +[K, V](kv: (K, V))(implicit ev: DriverType[K, V]): DriverMap = new DriverMap(underlying + kv)

  def -[K](k: K): DriverMap = new DriverMap(underlying - k)

  def get[K, V](k: K)(implicit ev: DriverType[K, V]): Option[V] = underlying.get(k).asInstanceOf[Option[V]]

  def isEmpty: Boolean = underlying.isEmpty

  def foldLeft = underlying.foldLeft _

}
