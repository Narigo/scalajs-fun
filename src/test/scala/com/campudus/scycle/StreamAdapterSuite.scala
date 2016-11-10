package com.campudus.scycle

import com.campudus.scycle.Scycle.{DisposeFunction, ScycleSubject, StreamAdapter, StreamSubscribe}
import org.scalatest.AsyncFunSpec
import rxscalajs.{Observable, Observer, Subject}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise
import scala.scalajs.js

class StreamAdapterSuite extends AsyncFunSpec {

  describe("StreamAdapter") {

    object RxJSAdapter extends StreamAdapter {

      override def adapt[T](
        originStream: Any,
        originStreamSubscribe: StreamSubscribe[T]
      ): Observable[T] = {
        if (this.isValidStream(originStream)) {
          originStream.asInstanceOf[Observable[T]]
        } else {
          Observable.create((observer: Observer[T]) => {
            val dispose = originStreamSubscribe(originStream, observer)
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
          override val stream: Any = _stream
          override val observer: Observer[T] = _observer
        }
      }
      override def isValidStream(stream: Any): Boolean = stream.isInstanceOf[Observable[_]]
      override def streamSubscribe[T]: StreamSubscribe[T] = myStreamSubscribe[T]

      private def myStreamSubscribe[T](stream: Any, observer: Observer[T]): DisposeFunction = {
        val subscription = stream.asInstanceOf[Observable[T]].subscribe(observer)
        val dispose: DisposeFunction = () => {
          subscription.unsubscribe()
        }
        dispose
      }

    }

    it("should adapt from a dummy adapter to this adapter stream") {
      val p = Promise[Int]()
      def arraySubscribe[A](array: Any, observer: Observer[A]): DisposeFunction = {
        val arr = array.asInstanceOf[Array[A]]
        arr.foreach(observer.next)
        observer.complete()
        () => {}
      }

      val dummyStream = Array(1, 2, 3)
      val rxStream = RxJSAdapter.adapt(dummyStream, arraySubscribe[Int])
      assert(RxJSAdapter.isValidStream(rxStream) === true)
      val expected = ListBuffer(1, 2, 3)
      rxStream.subscribe((x: Int) => {
        assert(x === expected.remove(0))
      }, (error: Any) => {
        p.failure(new RuntimeException(s"should not occur $error"))
      }, () => {
        assert(expected.length === 0)
        p.success(1)
      })
      p.future.map(i => assert(i === 1))
    }

    it("") {
      assert(false)
    }

  }

}
