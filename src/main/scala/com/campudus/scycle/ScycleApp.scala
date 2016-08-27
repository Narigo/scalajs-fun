package com.campudus.scycle

import com.campudus.scycle.dom._
import com.campudus.scycle.http.{Get, HttpDriver, Request}
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  implicit private val ctx = Ctx.Owner.safe()

  @JSExport
  def main(): Unit = {
    Scycle.run(logic, Map(
      "dom" -> makeDomDriver("#app"),
      "http" -> makeHttpDriver()
    ))
  }

  def logic(sources: collection.Map[String, Driver])(implicit ctx: Ctx.Owner): Rx[collection.Map[String, _]] = {
    Rx {
      val domDriver = sources("dom").asInstanceOf[DomDriver]
      val buttonClicks = domDriver.selectEvents(".get-first", "click")

      val request = Rx {
        val clickEvent = buttonClicks()
        println("button clicked -> Request!")
        Get("http://jsonplaceholder.typicode.com/users/1")
      }

      buttonClicks.triggerLater {
        println("button clicked")
      }

      Map(
        // Logic (functional)
        "dom" -> {
          Div(children = Seq(
            Button(className = "get-first", children = Seq(Text("Get first user"))),
            Div(className = "user-details", children = Seq(
              H1(className = "user-name", children = Seq(Text("(name)"))),
              Div(className = "user-email", children = Seq(Text("(email)"))),
              A(className = "user-website", href = "https://example.com", children = Seq(Text("(website)")))
            ))
          ))
        },
        "http" -> request()
      )
    }
  }

  def makeDomDriver(selector: String)(implicit ctx: Ctx.Owner): (Rx[_] => Driver) = {
    logicOut =>
      println("hello, DOM driver!")

      new DomDriver(selector, logicOut.asInstanceOf[Rx[Hyperscript]])(ctx)
  }

  def makeHttpDriver()(implicit ctx: Ctx.Owner): (Rx[_] => Driver) = {
    logicOut =>
      println("hello, HTTP driver!")

      new HttpDriver(logicOut.asInstanceOf[Rx[Request]])(ctx)
  }

}
