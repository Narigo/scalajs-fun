package com.campudus.scycle

import com.campudus.scycle.Scycle.DriverFunction
import org.scalatest.AsyncFunSpec
import rxscalajs.{Observable, Observer, Subject}

import scala.concurrent.Promise

class ScycleSuite extends AsyncFunSpec {

  describe("Scycle"){
    it("should not work with an empty main"){
      val thrown = intercept[IllegalArgumentException](Scycle.run(Map.empty, Map.empty))
      assert(thrown.getMessage.contains("driver"))
    }

    it("can read data from a logic"){
      val inputText = "Hello World!"
      val p = Promise[String]
      Scycle.run(
        drivers => {
          Map("test" -> Observable.just(inputText))
        },
        Map("test" -> {
          val df = new DriverFunction {

            def apply(stream: Observable[_], driverName: String): Observable[_] = {
              stream.map(t => {
                p.success(t.asInstanceOf[String])
              })
            }

          }
          df
        })
      )
      p.future.map(text => {
        assert(text === inputText)
      })
    }

    it("can cycle"){
      val p = Promise[Int]
      Scycle.run(drivers => {
        Map("test" -> {
          val testDriver = drivers("test")
          // TODO int$.map results in new observable which is not looked at in the test driver...
          testDriver.asInstanceOf[TestDriver].int$.map(p.success _).startWith(0)
        })
      },
        Map("test" -> new DriverFunction {

          override def apply(stream: Observable[_], driverName: String): Observable[_] = {
            val mapped = stream.map(i => {
              val nextI = i.asInstanceOf[Int] + 1
              nextI
            })
            val testDriver = new TestDriver(mapped, Subject[Int]())
            testDriver
          }

        })
      )
      p.future.map(i => assert(i === 1))
    }
  }

}

class TestDriver(input: Observable[Int], output: Observer[Int]) extends Driver(input, output) {

  val int$: Observable[Int] = {
    val sub = Subject[Int]()
    input.subscribe(sub)
    sub.subscribe(ev => {
    })
    sub
  }

  override def toString = s"TestDriver($input)"
}
