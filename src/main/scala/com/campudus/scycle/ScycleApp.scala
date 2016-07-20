package com.campudus.scycle

import org.scalajs.dom
import org.scalajs.dom.Event
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

sealed trait Hyperscript {
  def toElement: dom.Element
}

case class Text(text: String) extends Hyperscript {
  override def toElement: dom.Element = {
    val span = dom.document.createElement("span")
    val textNode = dom.document.createTextNode(text)
    span.appendChild(textNode)
    span
  }
}

class HyperScriptElement(tagName: String, subElements: Seq[Hyperscript]) extends Hyperscript {
  override def toElement: dom.Element = {
    val element = dom.document.createElement(tagName)
    subElements.foreach { child =>
      element.appendChild(child.toElement)
    }
    element
  }
}

case class H1(children: Hyperscript*) extends HyperScriptElement("h1", children)

case class Span(children: Hyperscript*) extends HyperScriptElement("span", children)

case class Div(children: Hyperscript*) extends HyperScriptElement("div", children)

sealed trait Driver

class ConsoleDriver extends Driver

class DomDriver(input: Rx[dom.Element])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing driver...")
  val container = dom.document.getElementById("app")

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

@JSExport
object ScycleApp extends JSApp {

  implicit private val ctx = Ctx.Owner.safe()

  @JSExport
  def main(): Unit = {
    Scycle.run(logic, Map(
      "dom" -> (logicOut => makeDomDriver(logicOut.asInstanceOf[Rx[dom.Element]])),
      "log" -> (logicOut => makeConsoleLogDriver(logicOut.asInstanceOf[Rx[String]]))
    ))
  }

  def logic(sources: collection.Map[String, Driver])(implicit ctx: Ctx.Owner): collection.Map[String, Rx[_]] = {
    Map(
      // Logic (functional)
      "dom" -> {
        val driver = sources("dom").asInstanceOf[DomDriver]
        val domSource = driver.selectEvents("h1", "click")
        val i = Var[Int](0)

        domSource.trigger {
          i() = 1
        }

        dom.setInterval(() => {
          i() = i.now + 1
        }, 1000)

        Rx {
          Div(
            H1(Text(s"Seconds elapsed ${i()} - domSource exists? ${domSource.now}")),
            Span(Text("Hello there..."))
          ).toElement
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

  def makeConsoleLogDriver(input: Rx[String])(implicit ctx: Ctx.Owner): Driver = {
    Rx {
      dom.console.log(input())
    }

    null
  }

  def makeDomDriver(input: Rx[dom.Element])(implicit ctx: Ctx.Owner): Driver = {
    println("hello, driver!")

    new DomDriver(input)
  }

}
