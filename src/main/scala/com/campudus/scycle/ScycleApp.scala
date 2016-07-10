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
        i() = i() + 1
      }, 1000)

      Rx(s"Seconds elapsed ${i()}")
    },
    "log" -> {
      val i = Var(0)
      dom.setInterval(() => {
        i() = i() * 2
      }, 2000)

      Rx(s"${i()}")
    }
  )

  def domEffect(text: Rx[String]): Unit = {
    Obs(text) {
      val container = dom.document.getElementById("app")
      container.textContent = text()
    }
  }

  def consoleLogEffect(log: Rx[String]): Unit = {
    Obs(log) {
      dom.console.log(log())
    }
  }

}


