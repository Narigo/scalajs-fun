package com.campudus.scycle

import com.campudus.scycle.Scycle.{DisposeFunction, ScycleSubject, StreamAdapter, StreamSubscribe}
import org.scalatest.AsyncFunSpec
import rxscalajs.{Observable, Observer, Subject}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.Any

class StreamAdapterSuite extends AsyncFunSpec {

  describe("StreamAdapter") {

    object RxJSAdapter extends StreamAdapter {

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

    it("should adapt from a dummy adapter to this adapter stream") {
      val p = Promise[Int]()
      def arraySubscribe[A](array: Observable[_], observer: Observer[A]): DisposeFunction = {
        val arr = array.asInstanceOf[Array[A]]
        arr.foreach(observer.next)
        observer.complete()
        () => {}
      }

      val dummyStream = Observable.just(1, 2, 3)
      val rxStream = RxJSAdapter.adapt[Int](dummyStream, arraySubscribe[Int])
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

    it("should create a subject which can be fed and subscribed to") {
      val subject = RxJSAdapter.makeSubject[Int]()
      assert(subject.stream.isInstanceOf[Subject[_]])
      assert(RxJSAdapter.isValidStream(subject.stream))

      val observer1Expected = mutable.Queue(1, 2, 3, 4)
      val observer2Expected = mutable.Queue(3, 4)

      RxJSAdapter.streamSubscribe(subject.stream, new Observer[Int] {
        override def next(x: Int) = assert(x === observer1Expected.dequeue())
        override def error(err: Any): Unit = fail("should not happen")
        override def complete(): Unit = assert(observer1Expected.length === 0)
      })

      subject.observer.next(1)
      subject.observer.next(2)

      RxJSAdapter.streamSubscribe(subject.stream, new Observer[Int] {
        override def next(x: Int) = assert(x === observer2Expected.dequeue())
        override def error(err: Any): Unit = fail("should not happen")
        override def complete(): Unit = assert(observer2Expected.length === 0)
      })

      subject.observer.next(3)
      subject.observer.next(4)
      subject.observer.complete()

      assert(true)
    }

  }

}
