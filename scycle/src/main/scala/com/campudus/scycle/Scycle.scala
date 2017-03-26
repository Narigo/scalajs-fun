package com.campudus.scycle

import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Observer}

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

  private def replicateMany(sinks: Sinks, sinkProxies: SinkProxiesMap): () => Unit = {

    type X = Any

    val disposeFunctions = sinks.keys
      .filter(name => {
        sinkProxies.underlying.exists(_._1 == name)
      })
      .map(driverKey => {
        implicit val driverType = driverKey.key
        val subs = sinks(driverKey).subscribe(sinkProxies(driverKey))
        val dispose = subs.unsubscribe _
        sinkProxies(driverKey).asInstanceOf[Observer[X]].next(null)
        dispose
      })

    () => disposeFunctions.foreach(_.apply())
  }

  private def logToConsoleError(error: Any): Unit = {
    println(error)
  }

  private def disposeSubscriptions(subscriptions: Map[DriverKey[_, _, _], AnonymousSubscription]): Unit = {
    subscriptions.foreach(_._2.unsubscribe())
  }

  private def callDrivers(
    drivers: DriversDefinition,
    sinkProxies: SinkProxiesMap
  ): Map[DriverKey[_, _, _], AnonymousSubscription] = {

    type X = Nothing

    drivers.underlying.foldLeft(Map[DriverKey[_, _, _], AnonymousSubscription]())({
      case (m, (name, value)) =>
        val key = name.asInstanceOf[DriverKey[_, _, _]]
        val driver = value.asInstanceOf[Driver[_]]
        val proxyObservable = sinkProxies(key).asInstanceOf[Observable[X]]
        val subscription = driver.subscribe(proxyObservable)
        m + (key -> subscription)
    })
  }

  private def makeSinkProxies(drivers: DriversDefinition): SinkProxiesMap = {
    drivers.underlying.foldLeft(new SinkProxiesMap())({
      case (m, (name, value)) =>
        val key = name.asInstanceOf[DriverKey[_, _, _]]
        val driver = value.asInstanceOf[Driver[_]]
        m + (key -> driver.createSubject())
    })
  }

}
