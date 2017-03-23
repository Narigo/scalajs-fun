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

  type DriversDefinition = DriverMap
  type Sources = DriverMap
  type Sinks = SinksMap

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

  def side[X](someNext: X => _): X => X = {
    x => {
      someNext(x)
      x
    }
  }

  private def replicateMany(sinks: Sinks, sinkProxies: Map[DriverKey, Subject[_]]): () => Unit = {

    type X = Any

    val disposeFunctions = sinks.keys
      .filter(name => {
        sinkProxies.exists(_._1 == name)
      })
      .map(driverKey => {
        val subs = sinks(driverKey).subscribe(sinkProxies(driverKey).asInstanceOf[Observer[X]])
        val dispose = subs.unsubscribe _
        sinkProxies(driverKey).asInstanceOf[Observer[X]].next(null)
        dispose
      })

    () => disposeFunctions.foreach(_.apply())
  }

  private def logToConsoleError(error: Any): Unit = {
    println(error)
  }

  private def disposeSubscriptions(subscriptions: Map[DriverKey, AnonymousSubscription]): Unit = {
    subscriptions.foreach(_._2.unsubscribe())
  }

  private def callDrivers(
    drivers: DriversDefinition,
    sinkProxies: Map[DriverKey, Subject[_]]
  ): Map[DriverKey, AnonymousSubscription] = {

    type X = Nothing

    drivers.underlying.foldLeft(Map[DriverKey, AnonymousSubscription]())({
      case (m, (name, value)) =>
        val key = name.asInstanceOf[DriverKey]
        val driver = value.asInstanceOf[Driver[_]]
        val proxyObservable = sinkProxies(key).asInstanceOf[Observable[X]]
        val subscription = driver.subscribe(proxyObservable)
        m + (key -> subscription)
    })
  }

  private def makeSinkProxies(drivers: DriversDefinition): Map[DriverKey, Subject[_]] = {
    drivers.underlying.foldLeft(Map[DriverKey, Subject[_]]())({
      case (m, (name, value)) =>
        val key = name.asInstanceOf[DriverKey]
        val driver = value.asInstanceOf[Driver[_]]
        m + (key -> driver.createSubject())
    })
  }

}
