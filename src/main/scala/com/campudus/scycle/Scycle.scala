package com.campudus.scycle

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

    if (drivers.isEmpty) {
      throw new IllegalArgumentException("Scycle needs at least one driver to work.")
    } else {
      val streamAdapter = null // FIXME this needs to be a real streamAdapter
      println("Scycle.run:sinkProxies")
      val sinkProxies = makeSinkProxies(drivers, streamAdapter)
      println("Scycle.run:sources")
      val sources = callDrivers(drivers, sinkProxies, streamAdapter).asInstanceOf[Sources]
      println("Scycle.run:sinks")
      val sinks = mainFn(sources)
      println("Scycle.run:replicateMany")
      val disposeReplication = replicateMany(sinks, sinkProxies, streamAdapter)

      val result = () => {
        disposeSources(sources)
        disposeReplication()
      }

      result
    }
  }

  private def replicateMany(
    sinks: Sinks,
    sinkProxies: Map[String, (Any, Observer[_])],
    streamAdapter: StreamAdapter
  ): () => Unit = {

    type X = Any

    println("in replicateMany")
    val disposeFunctions: List[DisposeFunction] = sinks.keys
      .filter(name => sinkProxies.exists(_._1 == name))
      .map(name => (name, streamAdapter.streamSubscribe))
      .foldLeft(List[DisposeFunction]()) {
        case (list, (name, fn)) =>
          println(s"fold left ($list, ($name, Some($fn)))")
          fn.apply(
            sinks(name).asInstanceOf[Observable[Nothing]], new Observer[Any] {

              override def next(x: Any): Unit = sinkProxies(name)._2.asInstanceOf[Observer[X]].next(x)

              override def error(err: scala.scalajs.js.Any): Unit = {
                logToConsoleError(err)
                sinkProxies(name)._2.asInstanceOf[Observer[X]].error(err)
              }

              override def complete(): Unit = sinkProxies(name)._2.asInstanceOf[Observer[X]].complete()

            }.asInstanceOf[Observer[Nothing]]
          ) :: list
        case (list, _) => list
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
    drivers.foldLeft(Map[String, Observable[_]]()) {
      case (m, (name, driverFn)) =>
        val driverOutput = driverFn(
          sinkProxies(name)._1.asInstanceOf[Observable[driverFn.In]],
          streamAdapter,
          name
        )

        val result = driverFn.streamAdapter.map(sa => {
          streamAdapter.adapt(
            driverOutput,
            sa.streamSubscribe
          )
        }).getOrElse(driverOutput)

        m + (name -> result)
    }
  }

  private def makeSinkProxies(
    drivers: DriversDefinition,
    streamAdapter: StreamAdapter
  ): Map[String, (Observable[_], Observer[_])] = {
    println("inner makeSinkProxies")
    drivers.foldLeft(Map[String, (Observable[_], Observer[_])]()) {
      case (m, (key, driver)) =>
        println("inner makeSinkProxies:foldLeft:holdSubject")
        val holdSubject = streamAdapter.makeSubject()
        println("inner makeSinkProxies:foldLeft:driverStreamAdapter")
        val driverStreamAdapter = drivers.get(key).flatMap(_.streamAdapter).getOrElse(streamAdapter)

        println("inner makeSinkProxies:foldLeft:stream")
        val stream = driverStreamAdapter.adapt(holdSubject.stream, streamAdapter.streamSubscribe)
        println("inner makeSinkProxies:foldLeft:observer")
        val observer = holdSubject.observer

        println("inner makeSinkProxies:foldLeft:return")
        m + (key -> (stream, observer))
    }
  }

}
