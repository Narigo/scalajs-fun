package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

class DomDriver(selector: String, input: Rx[Element])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing driver...")
  val container = document.querySelector(selector)

  Rx {
    container.innerHTML = input().outerHTML
  }

  def selectEvents(tagName: String, event: String)(implicit ctx: Ctx.Owner): Rx[Event] = {
    val eventVar = Var[Event](null)
    document.addEventListener(event, (e: Event) => {
      if (e.srcElement.tagName.toUpperCase() == tagName.toUpperCase()) {
        eventVar() = e
      }
    })
    eventVar
  }

}

