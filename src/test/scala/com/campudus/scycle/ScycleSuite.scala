package com.campudus.scycle

import com.campudus.scycle.Scycle.{DriverFunction, StreamAdapter}
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
      println("TEST START can read data from a logic")
      val inputText = "Hello World!"
      val p = Promise[String]
      Scycle.run(
        drivers => {
          println(s"ScycleSuite.main($drivers)")
          Map("test" -> {
            println(s"ScycleSuite.main($drivers):test")
            val obs = Observable.just(inputText)
            println(s"ScycleSuite.main($drivers):test -> return $obs")
            obs
          })
        },
        Map("test" -> {
          println(s"ScycleSuite.drivers(test)")
          val df = new DriverFunction[String, Unit] {

            def apply(stream: Observable[String], adapter: StreamAdapter, driverName: String): Observable[Unit] = {
              println(s"ScycleSuite.drivers(test):df($stream, $adapter, $driverName)")
              stream.map(t => {
                println(s"ScycleSuite.drivers(test):df($stream, $adapter, $driverName):map($t)")
                p.success(t)
              })
            }

          }
          println(s"ScycleSuite.drivers(test):return DriverFunction = $df")
          df
        })
      )
      println(s"ScycleSuite: Wait for future")
      p.future.map(text => {
        println("asserting text===inputText")
        assert(text === inputText)
      })
    }

    it("can cycle"){
      val p = Promise[Int]
      Scycle.run(drivers => {
        println(s"ScycleSuite:main($drivers)")
        Map("test" -> {
          val testDriver = drivers("test")
          println(s"ScycleSuite:main:int$$ of $testDriver")
          // TODO int$.map results in new observable which is not looked at in the test driver...
          val mapped = testDriver.asInstanceOf[TestDriver].int$.map(i => {
            println(s"ScycleSuite:main:testDriver.int  $$.map -> i=$i")
            p.success(i)
          }).startWith(0)
          println(s"ScycleSuite:main:mapped = $mapped")
          mapped
        })
      },
        Map("test" -> new DriverFunction[Int, Int] {

          override def apply(stream: Observable[Int], adapter: StreamAdapter, driverName: String): Observable[Int] = {
            val mapped = stream.map(i => {
              val nextI = i + 1
              println(s"ScycleSuite:drivers:stream.map -> i=$i, nextI=$nextI")
              nextI
            })
            val testDriver = new TestDriver(mapped, adapter.makeSubject[Int]())
            println(s"ScycleSuite:drivers:returning TestDriver = $testDriver")
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
      println(s"ScycleSuite:TestDriver:int$$:subscribe($ev)")
    })
    println(s"ScycleSuite:TestDriver:int$$:sub = $sub")
    sub
  }

  override def toString = s"TestDriver($input)"
}
