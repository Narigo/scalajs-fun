package com.campudus.scycle

import com.campudus.scycle.Scycle.DisposeFunction
import com.campudus.scycle.adapters.RxJsAdapter
import org.scalatest.AsyncFunSpec
import rxscalajs.{Observable, Observer, Subject}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise
import scala.scalajs.js.Any

class StreamAdapterSuite extends AsyncFunSpec {

  describe("StreamAdapter") {

    it("should adapt from a dummy adapter to this adapter stream") {
      val p = Promise[Int]()
      def arraySubscribe[A](array: Observable[_], observer: Observer[A]): DisposeFunction = {
        val arr = array.asInstanceOf[Array[A]]
        arr.foreach(observer.next)
        observer.complete()
        () => {}
      }

      val dummyStream = Observable.just(1, 2, 3)
      val rxStream = RxJsAdapter.adapt[Int](dummyStream, arraySubscribe[Int])
      assert(RxJsAdapter.isValidStream(rxStream) === true)
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
      val subject = RxJsAdapter.makeSubject[Int]()
      assert(subject.isInstanceOf[Subject[_]])
      assert(RxJsAdapter.isValidStream(subject))

      val observer1Expected = mutable.Queue(1, 2, 3, 4)
      val observer2Expected = mutable.Queue(3, 4)

      RxJsAdapter.streamSubscribe(subject, new Observer[Int] {
        override def next(x: Int) = assert(x === observer1Expected.dequeue())
        override def error(err: Any): Unit = fail("should not happen")
        override def complete(): Unit = assert(observer1Expected.length === 0)
      })

      subject.next(1)
      subject.next(2)

      RxJsAdapter.streamSubscribe(subject, new Observer[Int] {
        override def next(x: Int) = assert(x === observer2Expected.dequeue())
        override def error(err: Any): Unit = fail("should not happen")
        override def complete(): Unit = assert(observer2Expected.length === 0)
      })

      subject.next(3)
      subject.next(4)
      subject.complete()

      assert(true)
    }

  }

}
