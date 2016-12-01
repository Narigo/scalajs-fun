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

    def makeSubject[T](): Subject[T]

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
      println(s"Scycle.run:replicateMany(sinks=$sinks, sinkProxies=$sinkProxies, streamAdapter=$streamAdapter)")
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
    sinkProxies: Map[String, Subject[_]],
    streamAdapter: StreamAdapter
  ): () => Unit = {

    println(s"Scycle.replicateMany($sinks, $sinkProxies, $streamAdapter)")
    type X = Any

    val disposeFunctions = sinks.keys
      .filter(name => {
        println(s"Scycle.replicateMany:disposeFunctions:filter -> $name")
        sinkProxies.exists(_._1 == name)
      })
      .map(name => {
        println(s"Scycle.replicateMany:disposeFunctions:map -> $name")
        streamAdapter.streamSubscribe(
          sinks(name).asInstanceOf[Observable[Nothing]],
          sinkProxies(name).asInstanceOf[Observer[X]]
//          new Observer[X] {
//
//            override def next(x: X): Unit = {
//              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.next($x)")
//              sinkProxies(name).asInstanceOf[Observer[X]].next(x)
//              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.next($x) went into sinkProxy ${
//                sinkProxies(name)
//              }")
//            }
//
//            override def error(err: scala.scalajs.js.Any): Unit = {
//              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.err($err)")
//              logToConsoleError(err)
//              sinkProxies(name).asInstanceOf[Observer[X]].error(err)
//              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.err($err) went into sinkProxy ${
//                sinkProxies(name)
//              }")
//            }
//
//            override def complete(): Unit = {
//              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.complete()")
//              sinkProxies(name).asInstanceOf[Observer[X]].complete()
//              println(s"Scycle.replicateMany:disposeFunctions:map($name):Observer.complete() went into sinkProxy ${
//                sinkProxies(name)
//              }")
//            }
//
//          }.asInstanceOf[Observer[X]]
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
    sinkProxies: Map[String, Subject[_]],
    streamAdapter: StreamAdapter
  ): Map[String, Observable[_]] = {
    println(s"Scycle.callDrivers($drivers, $sinkProxies, $streamAdapter)")
    drivers.foldLeft(Map[String, Observable[_]]()){
      case (m, (name, driverFn)) =>
        println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn))")
        println(s"Scycle.callDrivers:driverFn(${sinkProxies(name)}, $streamAdapter, $name)")
        val driverOutput = driverFn(
          sinkProxies(name).asInstanceOf[Observable[Nothing]],
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

        result.subscribe(obs => {
          println(s"Scycle.callDrivers:result.subscribe -> $obs")
        })

        println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn)) -> add $name -> $result")
        m + (name -> result)
    }
  }

  private def makeSinkProxies(
    drivers: DriversDefinition,
    streamAdapter: StreamAdapter
  ): Map[String, Subject[_]] = {
    println("Scycle.makeSinkProxies")
    drivers.foldLeft(Map[String, Subject[_]]()){
      case (m, (key, driver)) =>
        println("Scycle.makeSinkProxies:foldLeft:holdSubject")
        val holdSubject = streamAdapter.makeSubject()
        println("Scycle.makeSinkProxies:foldLeft:driverStreamAdapter")
        val driverStreamAdapter = drivers.get(key).flatMap(_.streamAdapter).getOrElse(streamAdapter)

        println("Scycle.makeSinkProxies:foldLeft:stream")
        val stream = driverStreamAdapter.adapt(holdSubject, streamAdapter.streamSubscribe)
        println("Scycle.makeSinkProxies:foldLeft:observer")
        val observer = holdSubject

        println("Scycle.makeSinkProxies:foldLeft:return")
        m + (key -> holdSubject)
    }
  }

}
