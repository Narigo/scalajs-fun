package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Subject}

trait Driver[A] {

  def createSubject(): Subject[A] = Subject()

  def subscribe(inputs: Observable[A]): AnonymousSubscription = inputs.subscribe(_ => null)

}

class DriverKey(val key: DriverType[_, _, _])

class DriverType[K <: DriverKey, D <: Driver[_], V <: Observable[_]]

class DriverMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[K <: DriverKey, V <: Driver[_], _](kv: (K, V))
    (implicit ev: DriverType[K, V, _]): DriverMap = {
    new DriverMap(underlying + kv)
  }

  def -[K](k: K): DriverMap = new DriverMap(underlying - k)

  def apply[K <: DriverKey, V <: Driver[_], _](k: K)(implicit ev: DriverType[K, V, _]): V = get(k).get

  def get[K <: DriverKey, V <: Driver[_], _](k: K)(implicit ev: DriverType[K, V, _]): Option[V] = {
    println(s"DriverMap.get($k)")
    underlying.get(k).asInstanceOf[Option[V]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys[K <: DriverKey](implicit ev: DriverType[K, _, _]): Iterable[K] = {
    underlying.keys.map(k => k.asInstanceOf[K])
  }

}

class SinksMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[K <: DriverKey, V <: Observable[_]](kv: (K, V))
    (implicit ev: DriverType[K, _, V]): DriverMap = {
    new DriverMap(underlying + kv)
  }

  def -[K](k: K): SinksMap = new SinksMap(underlying - k)

  def apply[K <: DriverKey, _, V <: Observable[_]](k: K)(implicit ev: DriverType[K, _, V]): V = get(k).get

  def get[K <: DriverKey, _, V <: Observable[_]](k: K)(implicit ev: DriverType[K, _, V]): Option[V] = {
    println(s"SinksMap.get($k)")
    underlying.get(k).asInstanceOf[Option[V]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys: Iterable[DriverKey] = {
    underlying.keys.map(k => k.asInstanceOf[DriverKey])
  }

}
