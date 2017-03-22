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

  def apply[K <: DriverKey, V <: Driver[_]](k: K)(implicit ev: DriverType[K, V]): V = get(k).get

  def get[K <: DriverKey, V <: Driver[_]](k: K)(implicit ev: DriverType[K, V]): Option[V] = {
    println(s"DriverMap.get($k)")
    underlying.get(k).asInstanceOf[Option[V]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys[K <: DriverKey](implicit ev: DriverType[K, _]): Iterable[K] = {
    underlying.keys.map(k => k.asInstanceOf[K])
  }

}

class SinksType[K <: DriverKey, V <: Observable[_]]

class SinksMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[K <: DriverKey, V <: Observable[_]](kv: (K, V))
    (implicit ev: DriverType[K, V]): DriverMap = {
    new DriverMap(underlying + kv)
  }

  def -[K](k: K): SinksMap = new SinksMap(underlying - k)

  def apply[K <: DriverKey, V <: Observable[_]](k: K)(implicit ev: SinksType[K, V]): V = get(k).get

  def get[K <: DriverKey, V <: Observable[_]](k: K)(implicit ev: SinksType[K, V]): Option[V] = {
    println(s"SinksMap.get($k)")
    underlying.get(k).asInstanceOf[Option[V]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys[K <: DriverKey](implicit ev: SinksType[K, _]): Iterable[K] = {
    underlying.keys.map(k => k.asInstanceOf[K])
  }

}
