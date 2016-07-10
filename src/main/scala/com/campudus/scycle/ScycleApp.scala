package com.campudus.scycle

import org.scalajs.dom
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    val sinks = logic()
    domEffect(sinks("dom"))
    consoleLogEffect(sinks("log"))
  }

  def logic(): Map[String, Rx[String]] = Map(
    // Logic (functional)
    "dom" -> {
      val i = Var(0)
      dom.setInterval(() => {
        i() = i.now + 1
      }, 1000)

      Rx.unsafe(s"Seconds elapsed ${i()}")
    },
    "log" -> {
      val i = Var(0)
      dom.setInterval(() => {
        i() = i.now + 2
      }, 2000)

      Rx.unsafe(s"${i()}")
    }
  )

  def domEffect(text: Rx[String]): Unit = Rx.unsafe {
    val container = dom.document.getElementById("app")
    container.textContent = text()
  }

  def consoleLogEffect(log: Rx[String]): Unit = Rx.unsafe {
    dom.console.log(log())
  }

}


