package com.campudus.scycle

import com.campudus.scycle.Scycle.LogicOutput
import com.campudus.scycle.dom._
import org.scalajs.dom.{Element, console, html}
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.util.Random

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

        Rx {
          Div(children = Seq(
            Button(className = "decrement", children = Seq(Text("Decrement"))),
            Button(className = "increment", children = Seq(Text("Increment"))),
             P(children = Seq(
              Label(children = Seq(Text("0")))
            ))
          ))
        }
      }
    )
  }

  def makeDomDriver(selector: String)(implicit ctx: Ctx.Owner): (LogicOutput => Driver) = {
    logicOut =>
      println("hello, driver!")

      new DomDriver(selector, logicOut.asInstanceOf[Rx[Hyperscript]])
  }

}
