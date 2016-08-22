package com.campudus.scycle

import com.campudus.scycle.Scycle.LogicOutput
import com.campudus.scycle.dom._
import rx._

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
        val decrement = driver.selectEvents(".decrement", "click")
        val increment = driver.selectEvents(".increment", "click")

        val result = Var(0)

        Rx {
          decrement.triggerLater(result() = result() - 1)
          increment.triggerLater(result() = result() + 1)

          Div(children = Seq(
            Button(className = "decrement", children = Seq(Text("Decrement"))),
            Button(className = "increment", children = Seq(Text("Increment"))),
            P(children = Seq(
              Label(children = Seq(Text("" + result())))
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
