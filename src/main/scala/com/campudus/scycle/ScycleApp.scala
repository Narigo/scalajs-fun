package com.campudus.scycle

import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  implicit private val ctx = Ctx.Owner.safe()

  @JSExport
  def main(): Unit = {
    Scycle.run(logic, Map(
      "dom" -> domDriver _,
      "log" -> consoleLogDriver _
    ))
  }

  def logic(sources: collection.mutable.Map[String, Var[MouseEvent]])(implicit ctx: Ctx.Owner): Map[String, Rx[String]] = {
    val domSource = sources.getOrElseUpdate("dom", Var[MouseEvent](null))

    val i = Var[Int](0)
    Map(
      // Logic (functional)
      "dom" -> {

        domSource.trigger {
          i() = 1
        }

        dom.setInterval(() => {
          i() = i.now + 1
        }, 1000)

        Rx {
          s"Seconds elapsed ${i()} - domSource exists? ${domSource.now}"
        }
      },
      "log" -> {
        Rx {
          s"${i()} seconds elapsed since last click"
        }
      }
    )
  }

  def consoleLogDriver(input: Rx[String])(implicit ctx: Ctx.Owner): Var[MouseEvent] = {
    Rx {
      dom.console.log(input())
    }

    Var(null)
  }

  def domDriver(input: Rx[String])(implicit ctx: Ctx.Owner): Var[MouseEvent] = {
    Rx {
      val container = dom.document.getElementById("app")
      container.textContent = input()
    }

    val outRx = Var[MouseEvent](null)
    dom.document.addEventListener("click", { (ev: MouseEvent) =>
      outRx() = ev
    })
    outRx
  }

}


