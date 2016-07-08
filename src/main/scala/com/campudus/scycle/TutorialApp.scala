package com.campudus.scycle

import org.scalajs.dom
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object TutorialApp extends JSApp {

  @JSExport
  def main(): Unit = {
    val i = Var(0)
    val text = Rx {
      s"Seconds elapsed ${i()}"
    }

    dom.setInterval(() => {
      i() = i() + 1
      dom.console.log(text())
    }, 1000)

    dom.document.getElementById("app").textContent = text()
  }

}


