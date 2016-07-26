package com.campudus.scycle

import com.campudus.scycle.Scycle.LogicOutput
import com.campudus.scycle.dom._
import rx._

import org.scalajs.dom.{setInterval, Element}
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  implicit private val ctx = Ctx.Owner.safe()

  implicit def stringToTextNode(s: String): Hyperscript = Text(s)

  @JSExport
  def main(): Unit = {
    Scycle.run(logic, Map(
      "dom" -> makeDomDriver("#app")
    ))
  }

  def logic(sources: collection.Map[String, Driver])(implicit ctx: Ctx.Owner): collection.Map[String, Rx[_]] = {
    Map(
      // Logic (functional)
      "dom" -> {
        val driver = sources("dom").asInstanceOf[DomDriver]
        val domSource = driver.selectEvents("span", "click")
        val i = Var[Int](0)

        domSource.trigger {
          i() = 1
        }

        setInterval(() => {
          i() = i.now + 1
        }, 1000)

        Rx {
          Div(
            H1(s"Seconds elapsed ${i()} - domSource exists? ${domSource.now}"),
            Span("Hello there...")
          ).toElement
        }
      }
    )
  }

  def makeDomDriver(selector: String)(implicit ctx: Ctx.Owner): (LogicOutput => Driver) = {
    logicOut =>
      println("hello, driver!")

      new DomDriver(selector, logicOut.asInstanceOf[Rx[Element]])
  }

}
