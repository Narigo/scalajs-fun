package com.campudus.scycle

import org.scalatest.AsyncFunSpec
import rxscalajs.subscription.AnonymousSubscription
import rxscalajs.{Observable, Subject}

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
        Map("test" -> new Driver[String] {
          override def subscribe(inputs: Observable[String]): AnonymousSubscription = {
            inputs.subscribe(t => p.success(t))
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
        val testDriver = drivers("test").asInstanceOf[TestDriver]
        Map("test" -> {
          testDriver.int$.map(i => {
            p.success(i)
            i
          }).startWith(0)
        })
      },
        Map("test" -> new TestDriver)
      )
      p.future.map(i => assert(i === 1))
    }
  }

}

class TestDriver extends Driver[Int] {

  val int$: Subject[Int] = Subject[Int]()

  override def subscribe(inputs: Observable[Int]): AnonymousSubscription = {
    inputs.subscribe(i => {
      int$.next(i + 1)
    })
  }

  override def toString = s"TestDriver"

}
