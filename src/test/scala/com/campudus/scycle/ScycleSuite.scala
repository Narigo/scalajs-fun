package com.campudus.scycle

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
        _ => {
          Map("test" -> Observable.just(inputText))
        },
        Map("test" -> {
          (stream: Observable[String], driverName: String) => {
            stream.map(t => {
              p.success(t)
            })
          }
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
          testDriver.asInstanceOf[TestDriver].int$.map(p.success _).startWith(0)
        })
      },
        Map("test" -> {
          (stream: Observable[Int], driverName: String) => {
            val mapped = stream.map(i => {
              val nextI = i + 1
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
    sub.subscribe(_ => {})
    sub
  }

  override def toString = s"TestDriver($input)"
}
