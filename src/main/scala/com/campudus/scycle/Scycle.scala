package com.campudus.scycle

import rxscalajs.{Observable, Observer, _}

object Scycle {

  type DisposeFunction = () => Unit

  type StreamSubscribe[T] = (Observable[T], Observer[T]) => DisposeFunction

  trait ScycleSubject[T] {

    val stream: Observable[T]
    val observer: Observer[T]

  }

  trait DriverFunction[A, +B] extends ((Observable[A], String) => Observable[B]) {

    protected[this] def side[X](someNext: X => _): X => X = {
      x => {
        someNext(x)
        x
      }
    }

  }

  type DriversDefinition = Map[String, DriverFunction[_, _]]
  type Sources = Map[String, Observer[_]]
  type Sinks = Map[String, Observable[_]]

  def run(
    mainFn: Sources => Sinks,
    drivers: DriversDefinition
  ): () => Unit = {

    if (drivers.isEmpty) {
      throw new IllegalArgumentException("Scycle needs at least one driver to work.")
    } else {
      val sinkProxies = makeSinkProxies(drivers)
      val sources = callDrivers(drivers, sinkProxies).asInstanceOf[Sources]
      val sinks = mainFn(sources)
      val disposeReplication = replicateMany(sinks, sinkProxies)

      val result = () => {
        disposeSources(sources)
        disposeReplication()
      }

      result
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

  private def disposeSources(sources: Sources): Unit = {
    // TODO disposeSources
  }

  private def callDrivers(
    drivers: DriversDefinition,
    sinkProxies: Map[String, Subject[_]]
  ): Map[String, Observable[_]] = {

    type X = Nothing

    drivers.foldLeft(Map[String, Observable[_]]()){
      case (m, (name, driverFn)) =>
        val driverOutput = driverFn(
          sinkProxies(name).asInstanceOf[Observable[X]],
          name
        )

        driverOutput.subscribe(_ => {})

        m + (name -> driverOutput)
    }
  }

  private def makeSinkProxies(
    drivers: DriversDefinition
  ): Map[String, Subject[_]] = {
    drivers.foldLeft(Map[String, Subject[_]]()){
      case (m, (name, _)) =>
        val holdSubject = Subject()
        m + (name -> holdSubject)
    }
  }

}
