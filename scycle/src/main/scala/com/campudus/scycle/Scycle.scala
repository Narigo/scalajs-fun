package com.campudus.scycle

import com.campudus.scycle.dom.{DomDriver, Hyperscript}
import com.campudus.scycle.http.{HttpDriver, Request}
import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Observer, _}
import shapeless._

object Scycle {

  type DisposeFunction = () => Unit

  type StreamSubscribe[T] = (Observable[T], Observer[T]) => DisposeFunction

  trait ScycleSubject[T] {

    val stream: Observable[T]
    val observer: Observer[T]

  }

  type DriversDefinition = MyHMap[SourcesMapper]
  type Sources = MyHMap[SourcesMapper]
  type Sinks = MyHMap[SinkMapper]
  type SinkProxies = Map[Any, Subject[_]]

  def run(mainFn: Sources => Sinks, drivers: DriversDefinition): () => Unit = {

    if (drivers.isEmpty) {
      throw new IllegalArgumentException("Scycle needs at least one driver to work.")
    } else {
      val sinkProxies = makeSinkProxies(drivers)
      val subscriptions = callDrivers(drivers, sinkProxies)
      val sinks = mainFn(drivers)
      val disposeReplication = replicateMany(sinks, sinkProxies)

      val result = () => {
        disposeSubscriptions(subscriptions)
        disposeReplication()
      }

      result
    }
  }

  private def replicateMany(sinks: Sinks, sinkProxies: SinkProxies): () => Unit = {

    type X = Any

    val disposeFunctions = sinks.keys
      .filter(name => {
        sinkProxies.exists(a => {
          a._1 == name
        })
      })
      .map(name => {
        val subs = sinks(name).subscribe(sinkProxies(name).asInstanceOf[Observer[X]])
        val latest$ = sinks(name).lastOrElse(null)
        val dispose = subs.unsubscribe _
        latest$.map(x => {
          sinkProxies(name).asInstanceOf[Observer[X]].next(x)
        })
        dispose
      })

    () => disposeFunctions.foreach(_.apply())
  }

  private def disposeSubscriptions(subscriptions: Map[Any, AnonymousSubscription]): Unit = {
    subscriptions.foreach(_._2.unsubscribe())
  }

  private def callDrivers(
    drivers: DriversDefinition,
    sinkProxies: SinkProxies
  ): Map[Any, AnonymousSubscription] = {

    type X = Nothing

    drivers.foldLeft(Map[Any, AnonymousSubscription]()){
      case (m, (name, driver)) =>
        val proxyObservable = sinkProxies(name).asInstanceOf[Observable[X]]
        val subscription = driver.subscribe(proxyObservable)
        m + (name -> subscription)
    }
  }

  private def makeSinkProxies(drivers: DriversDefinition): SinkProxies = {
    drivers.foldLeft(Map[Any, Subject[_]]()){
      case (m, (name, driver)) =>
        m + (name -> driver.createSubject())
    }
  }

  class SinkMapper[K, +V <: Observable[_]]

  class SourcesMapper[K, +V <: Driver[_]]

  class MyHMap[R[_, _]](inner: HMap[R]) {

    var size = 0
    var keys = List[Any]()
    def apply[K, V](k: K)(implicit ev: R[K, V]): V = inner.get(k).get
    def isEmpty = size == 0
    def +[K, V](k: K, v: V)(implicit ev: R[K, V]) = {
      size += 1
      keys = k :: keys
      inner + (k, v)
    }
    def -[K, V](k: K, v: V)(implicit ev: R[K, V]) = {
      size -= 1
      keys = keys.filter(_ != k)
      inner - (k, v)
    }

  }

}
