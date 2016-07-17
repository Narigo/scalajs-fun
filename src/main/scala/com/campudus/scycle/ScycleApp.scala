package com.campudus.scycle

import org.scalajs.dom
import org.scalajs.dom.Event
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

sealed trait Driver

class ConsoleDriver extends Driver

class DomDriver(input: Rx[dom.Element])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing driver...")
  val container = dom.document.getElementById("app")
  val h1 = dom.document.createElement("h1")

  Rx {
    container.appendChild(input())
  }

  def selectEvents(tagName: String, event: String)(implicit ctx: Ctx.Owner): Rx[Event] = {
    val elements = dom.document.getElementsByTagName(tagName)
    val eventVar = Var[Event](null)
    for {
      i <- 0 until elements.length
    } {
      println(s"Add event listener to ${elements(i).nodeName}")
      elements(i).addEventListener(event, (e: Event) => {
        eventVar() = e
      })
    }
    eventVar
  }

  def h1(text: String): dom.Element = {
    h1.textContent = text
    h1
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
        val driver = sources("dom").asInstanceOf[DomDriver]
        val domSource = driver.selectEvents("div", "click")
        val i = Var[Int](0)

        domSource.trigger {
          i() = 1
        }

        dom.setInterval(() => {
          i() = i.now + 1
        }, 1000)

        Rx {
          driver.h1(s"Seconds elapsed ${i()} - domSource exists? ${domSource.now}")
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

  def domDriver(input: Rx[dom.Element])(implicit ctx: Ctx.Owner): Driver = {
    println("hello, driver!")

    new DomDriver(input)
  }

}
