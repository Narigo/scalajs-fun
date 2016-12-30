package com.campudus.scycle

import com.campudus.scycle.adapters.RxJsAdapter
import rxscalajs.{Observable, Observer, _}

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

    def streamSubscribe[T](a: Observable[T], b: Observer[T]): DisposeFunction

  }

  trait DriverFunction[A, B] extends ((Observable[A], String) => Observable[B]) {

    type In = A
    type Out = B

    def apply(stream: Observable[A], driverName: String): Observable[B]

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
      val streamAdapter = RxJsAdapter
      val sinkProxies = makeSinkProxies(drivers, streamAdapter)
      val sources = callDrivers(drivers, sinkProxies, streamAdapter).asInstanceOf[Sources]
      val sinks = mainFn(sources)
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
    sinkProxies: Map[String, Subject[_]],
    streamAdapter: StreamAdapter
  ): () => Unit = {

    type X = Any

    val disposeFunctions = sinks.keys
      .filter(name => {
        sinkProxies.exists(_._1 == name)
      })
      .map(name => {
        val dpf = streamAdapter.streamSubscribe(
          sinks(name).asInstanceOf[Observable[Nothing]],
          sinkProxies(name).asInstanceOf[Observer[X]]
        )
        sinkProxies(name).asInstanceOf[Observer[X]].next(null)
        dpf
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
    drivers.foldLeft(Map[String, Observable[_]]()){
      case (m, (name, driverFn)) =>
        val driverOutput = driverFn(
          sinkProxies(name).asInstanceOf[Observable[Nothing]],
          name
        )

        driverOutput.subscribe(_ => {})

        m + (name -> driverOutput)
    }
  }

  private def makeSinkProxies(
    drivers: DriversDefinition,
    streamAdapter: StreamAdapter
  ): Map[String, Subject[_]] = {
    drivers.foldLeft(Map[String, Subject[_]]()){
      case (m, (name, _)) =>
        val holdSubject = streamAdapter.makeSubject()
        m + (name -> holdSubject)
    }
  }

}
