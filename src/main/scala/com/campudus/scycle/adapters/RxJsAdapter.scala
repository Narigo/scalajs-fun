package com.campudus.scycle.adapters

import com.campudus.scycle.Scycle.{ScycleSubject, StreamAdapter, _}
import rxscalajs.{Observable, Observer, Subject}

import scala.scalajs.js

object RxJsAdapter extends StreamAdapter {

  override def adapt[T](
    originStream: Observable[_],
    originStreamSubscribe: StreamSubscribe[T]
  ): Observable[T] = {
    if (this.isValidStream[T](originStream)) {
      println(s"RxJsAdapter.adapt:isValidStream($originStream) = true")
      originStream.asInstanceOf[Observable[T]]
    } else {
      println(s"RxJsAdapter.adapt:isValidStream($originStream) = false")
      Observable.create((observer: Observer[T]) => {
        println(s"RxJsAdapter.adapt:Observable.create($observer).originStreamSubscribe")
        val dispose = originStreamSubscribe(originStream.asInstanceOf[Observable[T]], observer)
        println(s"RxJsAdapter.adapt:Observable.create($observer).dispose = $dispose")
        dispose
      })
    }
  }
  override def remember[T](stream: Observable[T]): Observable[T] = stream.publishReplay(1)
  override def makeSubject[T](): ScycleSubject[T] = {
    val _stream: Subject[T] = Subject[T]()
    val _observer: Observer[T] = new StreamObserver(_stream)
    new ScycleSubject[T] {
      override val stream: Observable[T] = _stream.asObservable()
      override val observer: Observer[T] = _observer
    }
  }
  override def isValidStream[T](stream: Observable[_]): Boolean = stream.isInstanceOf[Observable[T]]
  override def streamSubscribe[T]: StreamSubscribe[T] = myStreamSubscribe[T]

  private def myStreamSubscribe[T](stream: Observable[T], observer: Observer[T]): DisposeFunction = {
    println("RxJsAdapter.streamSubscribe:subscription")
    val subscription = stream.subscribe(observer)
    println(s"RxJsAdapter.streamSubscribe:dispose -> $subscription.unsubscribe")
    val dispose: DisposeFunction = () => {
      println(s"RxJsAdapter.streamSubscribe:dispose -> disposing streamSubscribe of $this")
      subscription.unsubscribe()
    }
    println(s"RxJsAdapter.streamSubscribe:return $dispose")
    dispose
  }

}

class StreamObserver[T](_stream: Observer[T]) extends Observer[T] {
  override def next(x: T): Unit = _stream.next(x)
  override def error(err: js.Any): Unit = _stream.error(err)
  override def complete(): Unit = _stream.complete()
}
