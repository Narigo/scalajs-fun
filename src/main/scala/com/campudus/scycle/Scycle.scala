package com.campudus.scycle

import rxscalajs._

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

  def run(
           mainFn: Map[String, Observer[_]] => Map[String, Observable[_]],
           drivers: DriversDefinition
         ): Unit = {

    if (drivers.nonEmpty) {
      val streamAdapter = null
      val sinkProxies = makeSinkProxies(drivers, streamAdapter)
      val sources = callDrivers(drivers, sinkProxies, streamAdapter)

      val effects = mainFn(sinkProxies.mapValues(_._2))
      effects.foreach({
        case (key, readEffect$) =>
          readEffect$.map({ ev =>
            println(s"main-in=$ev")
            ev
          }).subscribe({ ev =>
            println(s"got a new event in effect=$ev")
            feedIntoProxy(key, sinkProxies)(ev)
          })
      })
    }
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
