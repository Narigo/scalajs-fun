package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Observer, _}

object Scycle {

  type DisposeFunction = () => Unit

  type StreamSubscribe[T] = (Observable[T], Observer[T]) => DisposeFunction

  trait ScycleSubject[T] {

    val stream: Observable[T]
    val observer: Observer[T]

  }

  type DriversDefinition = SourcesMap
  type Sources = SourcesMap
  type Sinks = SinksMap
  type SinkProxies = Map[Any, Subject[_]]

  def run(mainFn: Sources => Sinks, drivers: DriversDefinition): () => Unit = {

    if (drivers.inner.isEmpty) {
      throw new IllegalArgumentException("Scycle needs at least one driver to work.")
    } else {
      val sinkProxies = makeSinkProxies(drivers)
      org.scalajs.dom.console.log("calling drivers")
      val subscriptions = callDrivers(drivers, sinkProxies)
      org.scalajs.dom.console.log("calling main")
      val sinks = mainFn(drivers)
      org.scalajs.dom.console.log("dispose setup")
      val disposeReplication = replicateMany(sinks, sinkProxies)

      val result = () => {
        disposeSubscriptions(subscriptions)
        disposeReplication()
      }

      org.scalajs.dom.console.log("result")
      result
    }
  }

  def side[X](someNext: X => _): X => X = {
    x => {
      someNext(x)
      x
    }
  }

  private def replicateMany(sinks: Sinks, sinkProxies: SinkProxies): () => Unit = {

    type X = Any

    val disposeFunctions = sinks.inner.keys
      .filter(name => {
        org.scalajs.dom.console.log(s"filtering sinks for $name")
        sinkProxies.exists(a => {
          org.scalajs.dom.console.log(s"is ${a._1} == $name ? ${a._1 == name}")
          a._1 == name
        })
      })
      .map(name => {
        org.scalajs.dom.console.log("got a sink")
        val subs = sinks.inner(name).subscribe(sinkProxies(name).asInstanceOf[Observer[X]])
        val latest$ = sinks.inner(name).lastOrElse(null)
        val dispose = subs.unsubscribe _
        latest$.map(x => {
          sinkProxies(name).asInstanceOf[Observer[X]].next(x)
        })
        dispose
      })

    () => disposeFunctions.foreach(_.apply())
  }

  private def logToConsoleError(error: Any): Unit = {
    println(error)
  }

  private def disposeSubscriptions(subscriptions: Map[Any, AnonymousSubscription]): Unit = {
    subscriptions.foreach(_._2.unsubscribe())
  }

  private def callDrivers(
    drivers: DriversDefinition,
    sinkProxies: SinkProxies
  ): Map[Any, AnonymousSubscription] = {

    type X = Nothing

    drivers.inner.foldLeft(Map[Any, AnonymousSubscription]()){
      case (m, (name, driver)) =>
        val proxyObservable = sinkProxies(name).asInstanceOf[Observable[X]]
        val subscription = driver.subscribe(proxyObservable)
        m + (name -> subscription)
    }
  }

  private def makeSinkProxies(drivers: DriversDefinition): SinkProxies = {
    org.scalajs.dom.console.log("test before makeSinkProxies.foldLeft")
    drivers.inner.foldLeft(Map[Any, Subject[_]]()){
      case (m, (name, driver)) =>
        org.scalajs.dom.console.log("test in makeSinkProxies.foldLeft")
        m + (name -> driver.createSubject())
    }
  }

  class SinkMapper[K, V <: Observable[_]]

  class SinksMap(val inner: Map[Any, Observable[_]] = Map.empty) {

    def get[K, V <: Observable[_]](k: K)(implicit ev: SinkMapper[K, V]): Option[V] = {
      inner.get(k).asInstanceOf[Option[V]]
    }

    def apply[K, V <: Observable[_]](k: K)(implicit ev: SinkMapper[K, V]): V = get(k).get

    def +[K, V <: Observable[_]](kv: (K, V))(implicit ev: SinkMapper[K, V]): SinksMap = new SinksMap(inner + kv)
    def -[K](k: K): SinksMap = new SinksMap(inner - k)
  }

  class SourcesMapper[K, V <: Driver[_]]

  class SourcesMap(val inner: Map[Any, Driver[_]] = Map.empty) {

    def get[K, V <: Driver[_]](k: K)(implicit ev: SourcesMapper[K, V]): Option[V] = inner.get(k).asInstanceOf[Option[V]]

    def apply[K, V <: Driver[_]](k: K)(implicit ev: SourcesMapper[K, V]): V = get(k).get

    def +[K, V <: Driver[_]](kv: (K, V))(implicit ev: SourcesMapper[K, V]): SourcesMap = new SourcesMap(inner + kv)
    def -[K](k: K): SourcesMap = new SourcesMap(inner - k)
  }

}
