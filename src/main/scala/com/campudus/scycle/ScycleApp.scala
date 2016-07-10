package com.campudus.scycle

import org.scalajs.dom
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  implicit private val ctx = Ctx.Owner.safe()

  @JSExport
  def main(): Unit = {
    run(logic, Map(
      "dom" -> domDriver,
      "log" -> consoleLogDriver
    ))
  }

  def run(mainFn: () => Map[String, Rx[String]], drivers: Map[String, Rx[String] => Unit]): Unit = {
    val sinks = mainFn()
    drivers foreach {
      case (key, fn) => fn(sinks(key))
    }
  }

  def logic()(implicit ctx: Ctx.Owner): Map[String, Rx[String]] = Map(
    // Logic (functional)
    "dom" -> {
      val i = Var(1)
      dom.setInterval(() => {
        i() = i.now + 1
      }, 1000)

      Rx(s"Seconds elapsed ${i()}")
    },
    "log" -> {
      val i = Var(1)
      dom.setInterval(() => {
        i() = i.now * 2
      }, 1000)

      Rx(s"${i()}")
    }
  )

  def domDriver(text: Rx[String]): Unit = Rx.unsafe {
    val container = dom.document.getElementById("app")
    container.textContent = text()
  }

  def consoleLogDriver(log: Rx[String]): Unit = Rx.unsafe {
    dom.console.log(log())
  }

}


