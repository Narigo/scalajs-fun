package com.campudus.scycle

import com.campudus.scycle.Scycle.{DisposeFunction, ScycleSubject, StreamAdapter, StreamSubscribe}
import org.scalatest.AsyncFunSpec
import rxscalajs.{Observable, Observer}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise

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
            dispose // TODO check that this is doing something!
          })
        }
      }
      override def remember[T](stream: Any): Any = ???
      override def makeSubject[T](): ScycleSubject[T] = ???
      override def isValidStream(stream: Any): Boolean = true
      override def streamSubscribe[T]: StreamSubscribe[T] = ???

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
        fail(s"should not occur $error")
      }, () => {
        assert(expected.length === 0)
        succeed
      })
      p.future.map(i => assert(i === 1))
    }

  }

}
