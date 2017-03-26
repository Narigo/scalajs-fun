package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Subject}

trait Driver[A] {

  def createSubject(): Subject[A] = Subject()

  def subscribe(inputs: Observable[A]): AnonymousSubscription = inputs.subscribe(_ => null)

}

class DriverKey[K, A](val key: DriverType[K, A])

class DriverType[K <: DriverKey[K, A], A <: Driver[A]]

class DriverMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[K <: DriverKey[K, A], A](kv: (K, Driver[A]))
    (implicit ev: DriverType[K, A]): DriverMap = {
    new DriverMap(underlying + kv)
  }

  def -[K](k: K): DriverMap = new DriverMap(underlying - k)

  def apply[A <: Driver[A], K <: DriverKey[K, A]](k: K)(implicit ev: DriverType[K, A]): Driver[A] = get(k).get

  def get[K <: DriverKey[K, A], A](k: K)(implicit ev: DriverType[K, A]): Option[A] = {
    println(s"DriverMap.get($k)")
    underlying.get(k).asInstanceOf[Option[A]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys: Iterable[DriverKey[_, _]] = {
    underlying.keys.map(k => k.asInstanceOf[DriverKey[_, _]])
  }

}

class SinksMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[K <: DriverKey[K, _], A](kv: (K, Observable[A]))
    (implicit ev: DriverType[K, A]): SinksMap = {
    new SinksMap(underlying + kv)
  }

  def -[K](k: K): SinksMap = new SinksMap(underlying - k)

  def apply[K <: DriverKey[K, _], A](k: K)(implicit ev: DriverType[K, A]): Observable[A] = get(k).get

  def get[K <: DriverKey[K, _], A](k: K)(implicit ev: DriverType[K, A]): Option[A] = {
    println(s"SinksMap.get($k)")
    underlying.get(k).asInstanceOf[Option[A]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys: Iterable[DriverKey[_, _]] = {
    underlying.keys.map(k => k.asInstanceOf[DriverKey[_, _]])
  }

}

class SinkProxiesMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[K <: DriverKey[K, _], A](kv: (K, Subject[A]))
    (implicit ev: DriverType[K, A]): SinkProxiesMap = {
    new SinkProxiesMap(underlying + kv)
  }

  def -[K](k: K): SinkProxiesMap = new SinkProxiesMap(underlying - k)

  def apply[K <: DriverKey[K, _], A](k: K)(implicit ev: DriverType[K, A]): Subject[A] = get(k).get

  def get[K <: DriverKey[K, _], A](k: K)(implicit ev: DriverType[K, A]): Option[A] = {
    println(s"SinksMap.get($k)")
    underlying.get(k).asInstanceOf[Option[A]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys: Iterable[DriverKey[_, _]] = {
    underlying.keys.map(k => k.asInstanceOf[DriverKey[_, _]])
  }

}
