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
      originStream.asInstanceOf[Observable[T]]
    } else {
      Observable.create((observer: Observer[T]) => {
        val dispose = originStreamSubscribe(originStream.asInstanceOf[Observable[T]], observer)
        dispose
      })
    }
  }
  override def remember[T](stream: Observable[T]): Observable[T] = stream.publishReplay(1)
  override def makeSubject[T](): ScycleSubject[T] = {
    val _stream: Subject[T] = Subject[T]()
    val _observer: Observer[T] = new Observer[T] {
      override def next(x: T): Unit = _stream.next(x)
      override def error(err: js.Any): Unit = _stream.error(err)
      override def complete(): Unit = _stream.complete()
    }
    new ScycleSubject[T] {
      override val stream: Observable[T] = _stream
      override val observer: Observer[T] = _observer
    }
  }
  override def isValidStream[T](stream: Observable[_]): Boolean = stream.isInstanceOf[Observable[T]]
  override def streamSubscribe[T]: StreamSubscribe[T] = myStreamSubscribe[T]

  private def myStreamSubscribe[T](stream: Observable[T], observer: Observer[T]): DisposeFunction = {
    val subscription = stream.asInstanceOf[Observable[T]].subscribe(observer)
    val dispose: DisposeFunction = () => {
      subscription.unsubscribe()
    }
    dispose
  }

}
