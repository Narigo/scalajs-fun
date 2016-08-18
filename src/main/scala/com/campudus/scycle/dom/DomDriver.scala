package com.campudus.scycle.dom

import com.campudus.scycle.Driver
import com.campudus.scycle.vdom.VirtualDom
import org.scalajs.dom._
import org.w3c.dom.Attr
import rx.{Ctx, Rx, Var}

class DomDriver(selector: String, input: Rx[Element])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing driver...")

  Rx {
    console.log("selecting again", document.querySelector("#app"))
    val container = document.querySelector(selector)
    val newChild = input()

    console.log("replacing", container, "with", newChild)

    console.log("hello??????????????")
    val before = VirtualDom(container)
    console.log(s"before:$before")
    val after = VirtualDom(newChild)
    console.log(s"after:$after")
    val diffs = VirtualDom.diff(before, after)
    console.log(s"to do update:$diffs")
    VirtualDom.update(container, diffs)
    console.log("updated")
//    if (oldChild.hasChildNodes()) {
//      oldChild.replaceChild(newChild.childNodes(3), oldChild.childNodes(3))
//    } else {
//      container.replaceChild(newChild, oldChild)
//    }
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

