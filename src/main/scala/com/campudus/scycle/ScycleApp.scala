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
    run(logic, Map(
      "dom" -> domDriver _
    ))
  }

  def run(
           mainFn: (collection.mutable.Map[String, Var[MouseEvent]]) => Map[String, Rx[String]],
           drivers: Map[String, (Rx[String], Var[MouseEvent]) => _]
         )(implicit ctx: Ctx.Owner): Unit = {
    val proxySources = collection.mutable.Map[String, Var[MouseEvent]]()
    val sinks = mainFn(proxySources)
    drivers foreach {
      case (key, fn) =>
        fn(sinks(key), proxySources(key))
    }
  }

  def logic(sources: collection.mutable.Map[String, Var[MouseEvent]])(implicit ctx: Ctx.Owner): Map[String, Rx[String]] = {
    val domSource = sources.getOrElseUpdate("dom", Var[MouseEvent](null))

    Map(
      // Logic (functional)
      "dom" -> {
        val i = Var[Int](0)

        domSource.trigger {
          i() = 1
        }

        dom.setInterval(() => {
          i() = i.now + 1
        }, 1000)

        Rx {
          s"Seconds elapsed ${i()} - domSource exists? ${domSource.now}"
        }
      }
    )
  }

  def domDriver(input: Rx[String], outRx: Var[MouseEvent])(implicit ctx: Ctx.Owner): Unit = Rx {
    val container = dom.document.getElementById("app")
    container.textContent = input()

    dom.document.addEventListener("click", { (ev: MouseEvent) =>
      outRx() = ev
    })
  }

}


