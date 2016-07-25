package com.campudus.scycle.driver

import org.scalajs.dom
import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

class DomDriver(selector: String, input: Rx[dom.Element])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing driver...")
  val container = dom.document.querySelector(selector)

  Rx {
    container.innerHTML = input().outerHTML
  }

  def selectEvents(tagName: String, event: String)(implicit ctx: Ctx.Owner): Rx[Event] = {
    val eventVar = Var[Event](null)
    dom.document.addEventListener(event, (e: Event) => {
      if (e.srcElement.tagName.toUpperCase() == tagName.toUpperCase()) {
        eventVar() = e
      }
    })
    eventVar
  }

}

