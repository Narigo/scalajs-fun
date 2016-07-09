package com.campudus.scycle

import org.scalajs.dom
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    // Logic (functional)
    val i = Var(0)
    dom.setInterval(() => {
      i() = i() + 1
    }, 1000)

    val text = Rx {
      s"Seconds elapsed ${i()}" // mapping the counter
    }

    // Effects (imperative)
    Obs(text) {
      val container = dom.document.getElementById("app")
      container.textContent = text()
    }
  }

}


