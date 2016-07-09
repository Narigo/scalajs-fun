package com.campudus.scycle

import org.scalajs.dom
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    val sink = logic()
    domEffect(sink)
    consoleLogEffect(sink)
  }

  def logic(): Rx[String] = {
    // Logic (functional)
    val i = Var(0)
    dom.setInterval(() => {
      i() = i() + 1
    }, 1000)

    Rx {
      s"Seconds elapsed ${i()}" // mapping the counter
    }
  }

  def domEffect(text: Rx[String]): Unit = {
    Obs(text) {
      val container = dom.document.getElementById("app")
      container.textContent = text()
    }
  }

  def consoleLogEffect(text: Rx[String]): Unit = {
    Obs(text) {
      dom.console.log(text())
    }
  }

}


