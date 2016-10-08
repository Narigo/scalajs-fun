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
      Scycle.run(drivers =>
        Map("test" -> Observable.just(inputText)),
        Map("test" -> { (sth$: Observable[_]) => sth$.map(text => p.success(text.asInstanceOf[String])) }))
      p.future.map(text => assert(text === inputText))
    }

  }

}
