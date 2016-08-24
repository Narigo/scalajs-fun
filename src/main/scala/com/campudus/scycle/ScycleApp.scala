package com.campudus.scycle

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

  def logic(sources: collection.Map[String, Driver])(implicit ctx: Ctx.Owner): Rx[collection.Map[String, _]] = {
    Rx {
      Map(
        // Logic (functional)
        "dom" -> {
          Div(children = Seq(
            Button(className = ".getFirst", children = Seq(Text("Get first user"))),
            Div(className = ".user-details", children = Seq(
              H1(className = ".user-name", children = Seq(Text("(name)"))),
              Div(className = ".user-email", children = Seq(Text("(email)"))),
              Div(className = ".user-website", children = Seq(Text("(website)")))
            ))
          ))
        }
      )
    }
  }

  def makeDomDriver(selector: String)(implicit ctx: Ctx.Owner): (Rx[_] => Driver) = {
    logicOut =>
      println("hello, driver!")

      new DomDriver(selector, logicOut.asInstanceOf[Rx[Hyperscript]])
  }

}
