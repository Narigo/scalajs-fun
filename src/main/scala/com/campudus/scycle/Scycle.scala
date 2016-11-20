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
      println("Scycle.run:sinkProxies")
      val sinkProxies = makeSinkProxies(drivers, streamAdapter)
      println("Scycle.run:sources")
      val sources = callDrivers(drivers, sinkProxies, streamAdapter).asInstanceOf[Sources]
      println("Scycle.run:sinks")
      val sinks = mainFn(sources)
      println("Scycle.run:replicateMany")
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

    val disposeFunctions: List[DisposeFunction] = sinks.keys
      .filter(name => {
        println(s"Scycle.replicateMany:disposeFunctions:filter -> $name")
        sinkProxies.exists(_._1 == name)
      })
      .map(name => {
        println(s"Scycle.replicateMany:disposeFunctions:map -> $name")
        (name, streamAdapter.streamSubscribe)
      })
      .foldLeft(List[DisposeFunction]()) {
        case (list, (name, fn)) =>
          println(s"Scycle.replicateMany:disposeFunctions:foldLeft($list, ($name, Some($fn))")
          val result = fn.apply(
            sinks(name).asInstanceOf[Observable[Nothing]], new Observer[Any] {

              override def next(x: Any): Unit = {
                println(s"Scycle.replicateMany:disposeFunctions:foldLeft($list, ($name, Some($fn)):Observer.next($x)")
                sinkProxies(name)._2.asInstanceOf[Observer[X]].next(x)
              }

              override def error(err: scala.scalajs.js.Any): Unit = {
                println(s"Scycle.replicateMany:disposeFunctions:foldLeft($list, ($name, Some($fn)):Observer.err($err)")
                logToConsoleError(err)
                sinkProxies(name)._2.asInstanceOf[Observer[X]].error(err)
              }

              override def complete(): Unit = {
                println(s"Scycle.replicateMany:disposeFunctions:foldLeft($list, ($name, Some($fn)):Observer.complete()")
                sinkProxies(name)._2.asInstanceOf[Observer[X]].complete()
              }

            }.asInstanceOf[Observer[Nothing]]
          )
          println(s"Scycle.replicateMany:disposeFunctions:foldLeft($list, ($name, Some($fn)):result = $result :: $list")
          result :: list
        case (list, ignored) =>
          println(s"Scycle.replicateMany:disposeFunctions:foldLeft($list, $ignored) -> case2")
          list
      }

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
  ): Any = {
    println("Scycle.callDrivers")
    drivers.foldLeft(Map[String, Observable[_]]()) {
      case (m, (name, driverFn)) =>
        println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn))")
        val driverOutput = driverFn(
          sinkProxies(name)._1.asInstanceOf[Observable[Nothing]], // FIXME scala doesnt allow this
          streamAdapter,
          name
        )

        println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn)):driverOutput = $driverOutput")
        val result = driverFn.streamAdapter.map(sa => {
          println(s"Scycle.callDrivers:foldLeft($m, ($name, $driverFn)):$driverFn:streamAdapter.map")
          streamAdapter.adapt(
            driverOutput,
            sa.streamSubscribe
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
    println("Scylce.makeSinkProxies")
    drivers.foldLeft(Map[String, (Observable[_], Observer[_])]()) {
      case (m, (key, driver)) =>
        println("Sycle.makeSinkProxies:foldLeft:holdSubject")
        val holdSubject = streamAdapter.makeSubject()
        println("Sycle.makeSinkProxies:foldLeft:driverStreamAdapter")
        val driverStreamAdapter = drivers.get(key).flatMap(_.streamAdapter).getOrElse(streamAdapter)

        println("Sycle.makeSinkProxies:foldLeft:stream")
        val stream = driverStreamAdapter.adapt(holdSubject.stream, streamAdapter.streamSubscribe)
        println("Sycle.makeSinkProxies:foldLeft:observer")
        val observer = holdSubject.observer

        println("Sycle.makeSinkProxies:foldLeft:return")
        m + (key -> (stream, observer))
    }
  }

}
