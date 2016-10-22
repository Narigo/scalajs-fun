package com.campudus.scycle

import rxscalajs._

import scala.scalajs.js.Any

object Scycle {

  type DisposeFunction = () => Unit

  type StreamSubscribe[T] = (Any, Observer[T]) => DisposeFunction

  trait ScycleSubject[T] {
    val stream: Any
    val observer: Observer[T]
  }

  trait StreamAdapter {
    def adapt[T](originStream: Any, originStreamSubscribe: Option[StreamSubscribe[T]]): Any

    def remember[T](stream: Any): Any

    def makeSubject[T](): ScycleSubject[T]

    def isValidStream(stream: Any): Boolean

    def streamSubscribe[T]: Option[StreamSubscribe[T]]
  }

  trait DriverFunction extends ((Any, StreamAdapter, String) => Any) {
    def apply(stream: Any, adapter: StreamAdapter, driverName: String): Any

    val streamAdapter: Option[StreamAdapter] = None
  }

  type DriversDefinition = Map[String, DriverFunction]
  type Sources = Map[String, Observer[_]]
  type Sinks = Map[String, Observable[_]]

  def run(
           mainFn: Sources => Sinks,
           drivers: DriversDefinition
         ): () => Unit = {

    if (drivers.nonEmpty) {
      val streamAdapter = null
      val sinkProxies = makeSinkProxies(drivers, streamAdapter)
      val sources = callDrivers(drivers, sinkProxies, streamAdapter).asInstanceOf[Sources]
      val sinks = mainFn(sources)
      val disposeReplication = replicateMany(sinks, sinkProxies, streamAdapter)

      () => {
        disposeSources(sources)
        disposeReplication()
      }
    } else {
      () => {}
    }
  }

  private def replicateMany(
                             sinks: Sinks,
                             sinkProxies: Map[String, (Any, Observer[_])],
                             streamAdapter: StreamAdapter
                           ): () => Unit = {

    type X = Any

    // TODO replicateMany
    val results = sinks.keys
      .filter(name => sinkProxies.exists(_._1 == name))
      .map(name => streamAdapter.streamSubscribe(sinks(name), new Observer[Any] {

        override def next(t: Any): Unit = sinkProxies(name)._2.asInstanceOf[Observer[X]].next(x)

        override def error(err: Any): Unit = {
          // TODO logToConsoleError(err)
          sinkProxies(name)._2.asInstanceOf[Observer[X]].error(err)
        }

        override def complete(): Unit = sinkProxies(name)._2.asInstanceOf[Observer[X]].complete()

      }))

    //TODO disposeFunctions
    //    val disposeFunctions = results.

    () => {
      //      disposeFunctions.forEach(dispose => dispose());
    }
  }

  private def disposeSources(sources: Sources): Unit = {
    // TODO disposeSources
  }

  private def callDrivers(
                           drivers: DriversDefinition,
                           sinkProxies: Map[String, (Any, Observer[_])],
                           streamAdapter: StreamAdapter
                         ): Any = {
    drivers.foldLeft(Map[String, Any]()) {
      case (m, (name, driverFn)) =>
        val driverOutput = driverFn(
          sinkProxies(name)._1,
          streamAdapter,
          name
        )

        val result = driverFn.streamAdapter.map({ sa =>
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
                             ): Map[String, (Any, Observer[_])] = {
    drivers.foldLeft(Map[String, (Any, Observer[_])]()) {
      case (m, (key, driver)) =>
        val holdSubject = streamAdapter.makeSubject()
        val driverStreamAdapter = drivers.get(key).flatMap(_.streamAdapter).getOrElse(streamAdapter)

        val stream = driverStreamAdapter.adapt(holdSubject.stream, streamAdapter.streamSubscribe)
        val observer = holdSubject.observer

        m + (key -> (stream, observer))
    }
  }

  private def feedIntoProxy[A](key: String, proxies: Map[String, (Any, Observer[_])])(event: A): Unit = {
    proxies(key)._2.asInstanceOf[Observer[A]].next(event)
  }

}
