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
    val domDriver = sources("dom").asInstanceOf[DomDriver]
    val buttonClicks1 = domDriver.selectEvents(".get-first", "click")
    val buttonClicks2 = domDriver.selectEvents(".abort-load", "click")

    val request = Rx {
      if (buttonClicks1() != null) {
        println("set request!")
        Some(Get("http://jsonplaceholder.typicode.com/users/1"))
      } else {
        println("no request to set!")
        None
      }
    }

    Rx {
      Map(
        // Logic (functional)
        "dom" -> {
          Div(children = Seq(
            Button(className = "get-first", children = Seq(Text(if (request().isEmpty) "Get first user" else "Getting first user"))),
            Button(className = "abort-load", children = Seq(Text(if (request().isEmpty) "Do nothing..." else "Stop loading"))),
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

      new HttpDriver(logicOut.asInstanceOf[Rx[Option[Request]]])(ctx)
  }

}
