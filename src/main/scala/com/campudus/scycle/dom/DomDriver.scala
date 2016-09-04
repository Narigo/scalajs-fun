package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import rx.{Ctx, Rx, Var}

class DomDriver(selector: String, input: Rx[Hyperscript])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing dom driver...")

  Rx {
    println("evaluating dom driver update of input")
    val container = document.querySelector(selector)
    val newChild = input()

    println(s"evaluating dom driver update of input - before $newChild")
    val before = VirtualDom(container)
    println("evaluating dom driver update of input - after")
    val after = Div(id = "app", children = Seq(newChild))
    println("evaluating dom driver update of input - diffs")
    val diffs = VirtualDom.diff(before, after)

    println(s"evaluating dom driver update of input - update $diffs")
    VirtualDom.update(container, diffs)
  }

  def selectEvents(selector: String, event: String)(implicit ctx: Ctx.Owner): Rx[Event] = {
    val eventVar = Var[Event](null)
    document.addEventListener(event, (e: Event) => {
      console.log("got an event", e)
      if (e.srcElement == document.querySelector(selector)) {
        console.log("pushing it through!")
        eventVar() = e
      } else {
        eventVar() = null
      }
    })
    eventVar
  }

}

