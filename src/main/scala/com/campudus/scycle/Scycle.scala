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

  type DriversDefinition = Map[String, Driver[_]]
  type Sources = Map[String, Driver[_]]
  type Sinks = Map[String, Observable[_]]

  def run(
    mainFn: Sources => Sinks,
    drivers: DriversDefinition
  ): () => Unit = {

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

  def side[X](someNext: X => _): X => X = {
    x => {
      someNext(x)
      x
    }
  }

  private def replicateMany(
    sinks: Sinks,
    sinkProxies: Map[String, Subject[_]]
  ): () => Unit = {

    type X = Any

    val disposeFunctions = sinks.keys
      .filter(name => {
        sinkProxies.exists(_._1 == name)
      })
      .map(name => {
        val subs = sinks(name).subscribe(sinkProxies(name).asInstanceOf[Observer[X]])
        val dispose = subs.unsubscribe _
        sinkProxies(name).asInstanceOf[Observer[X]].next(null)
        dispose
      })

    () => disposeFunctions.foreach(_.apply())
  }

  private def logToConsoleError(error: Any): Unit = {
    println(error)
  }

  private def disposeSubscriptions(subscriptions: Map[String, AnonymousSubscription]): Unit = {
    subscriptions.foreach(_._2.unsubscribe())
  }

  private def callDrivers(
    drivers: DriversDefinition,
    sinkProxies: Map[String, Subject[_]]
  ): Map[String, AnonymousSubscription] = {

    type X = Nothing

    drivers.foldLeft(Map[String, AnonymousSubscription]()){
      case (m, (name, driver)) =>
        val proxyObservable = sinkProxies(name).asInstanceOf[Observable[X]]
        val subscription = driver.subscribe(proxyObservable)
        m + (name -> subscription)
    }
  }

  private def makeSinkProxies(
    drivers: DriversDefinition
  ): Map[String, Subject[_]] = {
    drivers.foldLeft(Map[String, Subject[_]]()){
      case (m, (name, driver)) =>
        m + (name -> driver.createSubject())
    }
  }

}
