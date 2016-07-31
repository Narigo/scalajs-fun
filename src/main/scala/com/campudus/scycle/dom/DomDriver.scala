package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

class DomDriver(selector: String, input: Rx[Element])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing driver...")

  Rx {
    console.log("selecting again", document.querySelector("#app"))
    val container = document.querySelector(selector)
    val oldChild = container.firstChild
    val newChild = input()
    console.log("replacing", oldChild, "in", container, "with", newChild)

    container.replaceChild(newChild, oldChild)
  }

  def selectEvents(selector: String, event: String)(implicit ctx: Ctx.Owner): Rx[Event] = {
    val eventVar = Var[Event](null)
    document.addEventListener(event, (e: Event) => {
      console.log("got an event", e)
      if (e.srcElement == document.querySelector(selector)) {
        console.log("pushing it through!")
        eventVar() = e
      }
    })
    eventVar
  }

}

