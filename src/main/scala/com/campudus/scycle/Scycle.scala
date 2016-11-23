package com.campudus.scycle

import com.campudus.scycle.adapters.RxJsAdapter
import rxscalajs._

object Scycle {

  type DisposeFunction = () => Unit

  type StreamSubscribe[T] = (Observable[T], Observer[T]) => DisposeFunction

  trait ScycleSubject[T] {

    val stream: Observable[T]
    val observer: Observer[T]

  }

  trait StreamAdapter {

    def adapt[T](originStream: Observable[_], originStreamSubscribe: StreamSubscribe[T]): Observable[T]

    def remember[T](stream: Observable[T]): Observable[T]

    def makeSubject[T](): ScycleSubject[T]

    def isValidStream[T](stream: Observable[_]): Boolean

    def streamSubscribe[T]: StreamSubscribe[T]

  }

  trait DriverFunction[A, B] extends ((Observable[A], StreamAdapter, String) => Observable[B]) {

    type In = A
    type Out = B

    def apply(stream: Observable[A], adapter: StreamAdapter, driverName: String): Observable[B]

    val streamAdapter: Option[StreamAdapter] = None

  }

  type DriversDefinition = Map[String, DriverFunction[_, _]]
  type Sources = Map[String, Observer[_]]
  type Sinks = Map[String, Observable[_]]

  def run(
    mainFn: Sources => Sinks,
    drivers: DriversDefinition
  ): () => Unit = {

    println("Scycle.run:start")

    if (drivers.isEmpty) {
      throw new IllegalArgumentException("Scycle needs at least one driver to work.")
    } else {
      println("Scycle.run:streamAdapter")
      val streamAdapter = RxJsAdapter
      println(s"Scycle.run:sinkProxies - streamAdapter=$streamAdapter")
      val sinkProxies = makeSinkProxies(drivers, streamAdapter)
      println(s"Scycle.run:sources - sinkProxies=$sinkProxies")
      val sources = callDrivers(drivers, sinkProxies, streamAdapter).asInstanceOf[Sources]
      println(s"Scycle.run:sinks - sources=$sources")
      val sinks = mainFn(sources)
      println(s"Scycle.run:replicateMany - sinks=$sinks")
      val disposeReplication = replicateMany(sinks, sinkProxies, streamAdapter)

      println("Scycle.run:result")
      val result = () => {
        disposeSources(sources)
        disposeReplication()
      }

      println("Scycle.run:return result")
      result
    }
  }

  private def replicateMany(
    sinks: Sinks,
    sinkProxies: Map[String, (Observable[_], Observer[_])],
    streamAdapter: StreamAdapter
  ): () => Unit = {

    println("Scycle.replicateMany")
    type X = Any

    val disposeFunctions = sinks.keys
      .filter(name => {
        println(s"Scycle.replicateMany:disposeFunctions:filter -> $name")
        sinkProxies.exists(_._1 == name)
      })
      .map(name => {
        println(s"Scycle.replicateMany:disposeFunctions:map -> $name")
        streamAdapter.streamSubscribe(
          sinks(name).asInstanceOf[Observable[Nothing]], new Observer[X] {

            override def next(x: X): Unit = {
              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.next($x)")
              sinkProxies(name)._2.asInstanceOf[Observer[X]].next(x)
            }

            override def error(err: scala.scalajs.js.Any): Unit = {
              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.err($err)")
              logToConsoleError(err)
              sinkProxies(name)._2.asInstanceOf[Observer[X]].error(err)
            }

            override def complete(): Unit = {
              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.complete()")
              sinkProxies(name)._2.asInstanceOf[Observer[X]].complete()
            }

          }.asInstanceOf[Observer[X]]
        )
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
    sinkProxies: Map[String, (Observable[_], Observer[_])],
    streamAdapter: StreamAdapter
  ): Map[String, Observable[_]] = {
    println("Scycle.callDrivers")
    drivers.foldLeft(Map[String, Observable[_]]()) {
      case (m, (name, driverFn)) =>
        println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn))")
        val driverOutput = driverFn(
          sinkProxies(name)._1.asInstanceOf[Observable[Nothing]],
          streamAdapter,
          name
        )

        println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn)):driverOutput = $driverOutput")
        val driverStreamAdapter = driverFn.streamAdapter
        println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn)):driverStreamAdapter = $driverStreamAdapter")
        val result = driverStreamAdapter.map(dsa => {
          println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn)):$driverFn:streamAdapter.map")
          streamAdapter.adapt(
            driverOutput,
            dsa.streamSubscribe
          )
        }).getOrElse(driverOutput)

        println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn)) -> add $name -> $result")
        m + (name -> result)
    }
  }

  private def makeSinkProxies(
    drivers: DriversDefinition,
    streamAdapter: StreamAdapter
  ): Map[String, (Observable[_], Observer[_])] = {
    println("Scycle.makeSinkProxies")
    drivers.foldLeft(Map[String, (Observable[_], Observer[_])]()) {
      case (m, (key, driver)) =>
        println("Scycle.makeSinkProxies:foldLeft:holdSubject")
        val holdSubject = streamAdapter.makeSubject()
        println("Scycle.makeSinkProxies:foldLeft:driverStreamAdapter")
        val driverStreamAdapter = drivers.get(key).flatMap(_.streamAdapter).getOrElse(streamAdapter)

        println("Scycle.makeSinkProxies:foldLeft:stream")
        val stream = driverStreamAdapter.adapt(holdSubject.stream, streamAdapter.streamSubscribe)
        println("Scycle.makeSinkProxies:foldLeft:observer")
        val observer = holdSubject.observer

        println("Scycle.makeSinkProxies:foldLeft:return")
        m + (key -> (stream, observer))
    }
  }

}
