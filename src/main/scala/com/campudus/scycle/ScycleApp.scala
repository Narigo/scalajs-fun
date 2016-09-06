package com.campudus.scycle

import com.campudus.scycle.dom._
import com.campudus.scycle.http.{Get, HttpDriver, NonRequest, Request}
import rx._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.util.Random

@JSExport
object ScycleApp extends JSApp {

  val mainCtx = Ctx.Owner.safe()

  @JSExport
  def main(): Unit = {
    val ctx = mainCtx
    println(s"owner in ScycleApp.main = $ctx")
    Scycle.run(logic(_)(ctx), Map(
      "dom" -> makeDomDriver("#app"),
      "http" -> makeHttpDriver()
    ))(ctx)
  }

  def logic(sources: collection.Map[String, Driver])(implicit ctx: Ctx.Owner): Rx[collection.Map[String, _]] = {
    val domDriver = sources("dom").asInstanceOf[DomDriver]
    val httpDriver = sources("http").asInstanceOf[HttpDriver]
    val buttonClicks1 = domDriver.selectEvents(".get-first", "click")
    val buttonClicks2 = domDriver.selectEvents(".abort-load", "click")
    val response = httpDriver.getResponse()
    var testEv: org.scalajs.dom.Event = null

    println(s"owner in app.logic = $ctx")

    val request = Rx {
      println("evaluating request in app")
      println(s"owner in app.request = $ctx")
      val clickEv = buttonClicks1()
      testEv = clickEv
      if (clickEv != null) {
        Get(s"http://jsonplaceholder.typicode.com/users/${Random.nextInt()}")
      } else {
        NonRequest
      }
    }

    println(s"current request ${request.now}")

    Rx {
      println("evaluating map. Something changed!")
      println(s"owner in Scycle.App map = $ctx")

      val user = {
        val userResponse = response()
        println("evaluating user in app")
        println(s"response is now $userResponse")
        if (userResponse != null) {
          val u = userResponse.split(" ")
          User(u(0), u(1), u(2))
        } else {
          User("(name)", "(email)", "(website)")
        }
      }

      println(s"in map, user = $user")

      Map(
        "dom" -> {
          Div(children = Seq(
            Button(className = "get-first", children = Seq(Text(if (request() == null) "Get first user" else "Getting first user"))),
            Button(className = "abort-load", children = Seq(Text(if (request() == null) "Do nothing..." else "Stop loading"))),
            Div(className = "user-details", children = Seq(
              //              H1(className = "user-name", children = Seq(Text("(name)"))),
              //              Div(className = "user-email", children = Seq(Text("(email)"))),
              //              A(className = "user-website", href = "(website)", children = Seq(Text("(website)")))
              H1(className = "user-name", children = Seq(Text(s"${user.name}"))),
              Div(className = "user-email", children = Seq(Text(s"${user.email}"))),
              A(className = "user-website", href = s"${user.website}", children = Seq(Text(s"${user.website}")))
            ))
          ))
        },
        "http" -> {
          println(s"current request in app ${request.now}")
          request()
        }
      )
    }
  }

  def makeDomDriver(selector: String)(ctx: Ctx.Owner): (Rx[_] => Driver) = {
    logicOut =>
      println("hello, DOM driver!")

      new DomDriver(selector, logicOut.asInstanceOf[Rx[Hyperscript]])(ctx)
  }

  def makeHttpDriver()(ctx: Ctx.Owner): (Rx[_] => Driver) = {
    logicOut =>
      println(s"hello, HTTP driver! Got $logicOut")
      println(s"owner in makeHttpDriver = $ctx")
      implicit val context = ctx
      println(s"owner in makeHttpDriver/implicit = $context")

      val input = Rx {
        println("evaluating input in makeHttpDriver")
        println(s"owner in makeHttpDriver.input = $ctx")
        val in = logicOut()
        println("received an in")
        println(s"in = $in")
        val value = if (in == null) {
          NonRequest
        } else {
          in.asInstanceOf[Request]
        }
        println(s"value set done")
        value
      }

      println(s"creating httpdriver with ${input.now}")
      new HttpDriver(input)(ctx)

  }

}

case class User(name: String, email: String, website: String)
