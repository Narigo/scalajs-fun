package com.campudus.scycle

import com.campudus.scycle.Scycle.{DriverFunction, StreamAdapter}
import org.scalatest.AsyncFunSpec
import rxscalajs.{Observable, Subject}

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
      println("before run")
      Scycle.run(
        drivers => {
          println(s"main function returning a map from $drivers")
          Map("test" -> {
            println("main function, returning Observable.just(inputText)")
            val obs = Observable.just(inputText)
            println("created observable in main fn, returning it")
            obs
          })
        },
        Map("test" -> {
          println("driver map, creating new DriverFunction")
          val df = new DriverFunction {

            def apply(stream: scala.Any, adapter: StreamAdapter, driverName: String): scala.Any = {
              stream.asInstanceOf[Observable[String]].map({ t => p.success(t) })
              null
            }

          }
          println("DriverFunction created in driver map, returning it")
          df
        })
      )
      println("wait for future")
      p.future.map(text => {
        println("asserting text===inputText")
        assert(text === inputText)
      })
    }

    it("can cycle") {
      println("cycle test")
      val p = Promise[Int]
      Scycle.run(
        { drivers => {
          {
            println(s"main got drivers $drivers")
            Map("test" -> {
              val testDriver = drivers("test")
              println(s"got a testDriver? $testDriver")
              // TODO int$.map results in new observable which is not looked at in the test driver...
              val mapped = testDriver.asInstanceOf[TestDriver].int$.map({ i => {
                {
                  {
                    println(s"test-main-map-i=$i")
                    p.success(i)
                  }
                }
              }
              }).startWith(0)
              println(s"main done $mapped")
              mapped
            })
          }
        }
        },
        Map("test" -> new DriverFunction {

          override def apply(stream: scala.Any, adapter: StreamAdapter, driverName: String): scala.Any = {
            val mapped = stream.asInstanceOf[Observable[Int]].map({ i => {
              {
                println(s"test-driver-map-i=$i")
                val nextI = i + 1
                println(s"nextI=$nextI")
                nextI
              }
            }
            })
            println(s"call new Driver with $mapped")
            new TestDriver(mapped)
          }

        })
      )
      p.future.map(i => assert(i === 1))
    }
  }

}

class TestDriver(input: Observable[Int]) extends Driver(input) {

  val int$: Observable[Int] = {
    val sub = Subject[Int]()
    input.subscribe(sub)
    sub.subscribe(ev => {
      println(s"hello sub int$$=$ev")
    })
    sub
  }

  override def toString = s"TestDriver($input)"
}
