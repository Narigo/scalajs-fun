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

    val streamSubscribe: Option[StreamSubscribe[_]]
  }

  trait DriverFunction {
    def apply(stream: Any, adapter: StreamAdapter, driverName: String): Any

    val streamAdapter: Option[StreamAdapter] = None
  }

  def run(
           mainFn: Map[String, Observer[_]] => Map[String, Observable[_]],
           drivers: Map[String, DriverFunction]
         ): Unit = {

    if (drivers.nonEmpty) {
      val sinkProxies = makeSinkProxies(drivers, null)

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

  private def makeSinkProxies(
                               drivers: Map[String, DriverFunction],
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

  private def createProxy[A](driverFn: Observable[A] => Driver[_]): (Subject[A], Driver[_]) = {
    val proxy = Subject[A]()
    (proxy, driverFn(proxy))
  }

  private def feedIntoProxy[A](key: String, proxies: Map[String, (Any, Observer[_])])(event: A): Unit = {
    proxies(key)._2.asInstanceOf[Observer[A]].next(event)
  }

}
