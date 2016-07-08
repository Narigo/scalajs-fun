package com.campudus.scycle

import org.scalajs.dom
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object TutorialApp extends JSApp {

  @JSExport
  def main(): Unit = {
    val container = dom.document.getElementById("app")
    val i = Var(0)
    val text = Rx {
      s"Seconds elapsed ${i()}"
    }

    val iObserver = Obs(text) {
      container.textContent = text()
    }

    dom.setInterval(() => {
      i() = i() + 1
    }, 1000)
  }

}


