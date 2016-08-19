package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

class DomDriver(selector: String, input: Rx[Hyperscript])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing driver...")

  Rx {
    console.log("selecting again", document.querySelector("#app"))
    val container = document.querySelector(selector)
    val newChild = input()

    val before = VirtualDom(container)
    val after = Div(id = "app", children = Seq(newChild))
    val diffs = VirtualDom.diff(before, after)

    VirtualDom.update(container, diffs)
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

