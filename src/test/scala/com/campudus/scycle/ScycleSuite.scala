package com.campudus.scycle

import org.scalatest.AsyncFunSpec
import rxscalajs.Observable

import scala.concurrent.Promise

class ScycleSuite extends AsyncFunSpec {

  describe("Scycle") {
    it("works with empty maps") {
      Scycle.run(Map.empty, Map.empty)
      succeed
    }

    it("can read data from a logic") {
      val inputText = "Hello World!"
      val p = Promise[String]
      Scycle.run(
        drivers => Map("test" -> Observable.just(inputText)),
        Map("test" -> { (sth$: Observable[_]) => sth$.map(text => p.success(text.asInstanceOf[String])) })
      )
      p.future.map(text => assert(text === inputText))
    }

    it("can cycle") {
      val p = Promise[Int]
      Scycle.run(
        drivers => Map("test" -> drivers("test").asInstanceOf[Observable[Int]].map({ i =>
          println(s"test-main-map-i=$i")
          p.success(i)
        }).startWith(0)),
        Map("test" -> { (sth$: Observable[_]) =>
          sth$.map({ i =>
            println(s"test-driver-i=$i")
            i.asInstanceOf[Int] + 1
          })
        })
      )
      p.future.map(i => assert(i === 1))
    }
  }

}
