package com.campudus.scycle

import com.campudus.scycle.Scycle.LogicOutput
import com.campudus.scycle.dom._
import org.scalajs.dom.{Element, console, html}
import rx._

import scala.scalajs.js
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
        val domSource = driver.selectEvents(".field", "input")

        Rx {
          val name: String = Option(domSource()).map(_.srcElement.asInstanceOf[html.Input].value).getOrElse("")
          console.log("domSource", domSource(), "name=", name)

          Div(children = Seq(
            Label(children = Seq("Name:")),
            Input(className = "field", kind = "text", value = name),
            Hr(),
            H1(children = Seq(s"Hello $name!"))
          )).toElement
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
