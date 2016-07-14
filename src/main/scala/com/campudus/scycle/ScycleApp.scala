package com.campudus.scycle

import org.scalajs.dom
import org.scalajs.dom.Event
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

sealed trait Driver

class ConsoleDriver extends Driver

class DomDriver extends Driver {
  def selectEvents(tagName: String, event: String)(implicit ctx: Ctx.Owner): Rx[Event] = {
    println("hello, select events!")
    val elements = dom.document.getElementsByTagName(tagName)
    val eventVar = Var[Event](null)
    for {
      i <- 0 until elements.length
    } {
      elements(i).addEventListener(event, (e: Event) => {
        eventVar() = e
      })
    }
    eventVar
  }
}

@JSExport
object ScycleApp extends JSApp {

  implicit private val ctx = Ctx.Owner.safe()

  @JSExport
  def main(): Unit = {
    Scycle.run(logic, Map(
      "dom" -> (logicOut => domDriver(logicOut.asInstanceOf[Rx[dom.Element]])),
      "log" -> (logicOut => consoleLogDriver(logicOut.asInstanceOf[Rx[String]]))
    ))
  }

  def logic(sources: collection.Map[String, Driver])(implicit ctx: Ctx.Owner): collection.Map[String, Rx[_]] = {
    Map(
      // Logic (functional)
      "dom" -> {
        println("hello, dom logic!")

        val domSource = sources("dom").asInstanceOf[DomDriver].selectEvents("div", "click")
        val i = Var[Int](0)

        domSource.trigger {
          i() = 1
        }

        dom.setInterval(() => {
          i() = i.now + 1
        }, 1000)

        val h1 = dom.document.createElement("h1")
        Rx {
          h1.textContent = s"Seconds elapsed ${i()} - domSource exists? ${domSource.now}"
          h1
        }
      },
      "log" -> {
        val i = Var[Int](0)

        dom.setInterval(() => {
          i() = i.now + 1
        }, 1000)

        Rx {
          s"${i()} seconds elapsed since page opened"
        }
      }
    )
  }

  def consoleLogDriver(input: Rx[String])(implicit ctx: Ctx.Owner): Driver = {
    Rx {
      dom.console.log(input())
    }

    null
  }

  def domDriver(input: Rx[dom.Element])(implicit ctx: Ctx.Owner): DomDriver = {
    println("hello, driver!")

    Rx {
      val container = dom.document.getElementById("app")
      container.appendChild(input())
    }

    new DomDriver
  }

}
