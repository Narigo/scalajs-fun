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
      Scycle.run({ drivers =>
        Map(
          "text" -> {
            println("test main???")
            val input$ = Observable.just(inputText)
            println(s"observable.main=${input$}")
            input$
          }
        )
      },
        Map(
          "text" -> { (sth$: Observable[_]) =>
            println("test driver???")
            println(s"observable.driver=${sth$}")
            val mapped = sth$.map({ text =>
              println("mapped!")
              p.success(text.asInstanceOf[String])
            })
            mapped.subscribe(_ => {})
            mapped
          }
        )
      )
      p.future.map(text => assert(text === inputText))
    }
  }

}
