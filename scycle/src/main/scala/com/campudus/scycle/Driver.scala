package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Subject}

trait Driver[A] {

  def createSubject(): Subject[A] = Subject()

  def subscribe(inputs: Observable[A]): AnonymousSubscription = inputs.subscribe(_ => null)

}

class DriverKey[Key, Value, DriverType <: Driver[Value]]

class DriverMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[Key, DriverType](kv: (Key, DriverType))
    (implicit ev: DriverKey[Key, _, DriverType]): DriverMap = {
    new DriverMap(underlying + kv)
  }

  def -[Key](k: Key): DriverMap = new DriverMap(underlying - k)

  def apply[Key <: DriverKey[Key, Value, DriverType], Value, DriverType](k: Key)
    (implicit ev: DriverKey[Key, Value, DriverType]): DriverType = {
    get(k).get
  }

  def get[Key <: DriverKey[Key, Value, DriverType], Value, DriverType](k: Key)
    (implicit ev: DriverKey[Key, Value, DriverType]): Option[DriverType] = {
    println(s"DriverMap.get($k)")
    underlying.get(k).asInstanceOf[Option[DriverType]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys: Iterable[DriverKey[_, _, _]] = {
    underlying.keys.map(k => k.asInstanceOf[DriverKey[_, _, _]])
  }

  def filter[Key <: DriverKey[_, _, _]](predicate: (Key, Any) => Boolean): DriverMap = {
    new DriverMap(underlying.filter({
      case (key, value) => predicate(key.asInstanceOf[Key], value)
    }))
  }

}

class SinksMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[Key, Value](kv: (Key, Observable[Value]))
    (implicit ev: DriverKey[Key, Value, _]): SinksMap = {
    new SinksMap(underlying + kv)
  }

  def -[Key](k: Key): SinksMap = new SinksMap(underlying - k)

  def apply[Key <: DriverKey[Key, Value, DriverType], Value, DriverType](k: Key)
    (implicit ev: DriverKey[Key, Value, DriverType]): Observable[Value] = {
    get(k).get
  }

  def get[Key <: DriverKey[Key, Value, DriverType], Value, DriverType](k: Key)
    (implicit ev: DriverKey[Key, Value, DriverType]): Option[Observable[Value]] = {
    println(s"SinksMap.get($k)")
    underlying.get(k).asInstanceOf[Option[Observable[Value]]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys[Key <: DriverKey[_, _, _]]: Iterable[Key] = {
    underlying.keys.map(k => k.asInstanceOf[Key])
  }

  def filter[Key <: DriverKey[_, _, _]](predicate: (Key, Any) => Boolean): SinksMap = {
    new SinksMap(underlying.filter({
      case (key, value) => predicate(key.asInstanceOf[Key], value)
    }))
  }

}

class SinkProxiesMap(val underlying: Map[Any, Any] = Map.empty) {

  def +[Key, Value](kv: (Key, Subject[Value]))
    (implicit ev: DriverKey[Key, Value, _]): SinkProxiesMap = {
    new SinkProxiesMap(underlying + kv)
  }

  def -[Key](k: Key): SinkProxiesMap = new SinkProxiesMap(underlying - k)

  def apply[Key <: DriverKey[Key, Value, DriverType], Value, DriverType](k: Key)
    (implicit ev: DriverKey[Key, Value, DriverType]): Subject[Value] = {
    get(k).get
  }

  def get[Key <: DriverKey[Key, Value, DriverType], Value, DriverType](k: Key)
    (implicit ev: DriverKey[Key, Value, DriverType]): Option[Subject[Value]] = {
    println(s"SinkProxiesMap.get($k)")
    underlying.get(k).asInstanceOf[Option[Subject[Value]]]
  }

  def isEmpty: Boolean = underlying.isEmpty

  def keys: Iterable[DriverKey[_, _, _]] = {
    underlying.keys.map(k => k.asInstanceOf[DriverKey[_, _, _]])
  }

  def filter[Key <: DriverKey[_, _, _]](predicate: (Key, Any) => Boolean): SinkProxiesMap = {
    new SinkProxiesMap(underlying.filter({
      case (key, value) => predicate(key.asInstanceOf[Key], value)
    }))
  }

}
