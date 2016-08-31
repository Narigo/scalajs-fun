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
    val httpDriver = sources("http").asInstanceOf[HttpDriver]
    val buttonClicks1 = domDriver.selectEvents(".get-first", "click")
    val buttonClicks2 = domDriver.selectEvents(".abort-load", "click")
    println("getting response")
    val response = httpDriver.getResponse()
    println(s"got response as Rx[String]")

    val request = Rx {
      println("evaluating request")
      if (buttonClicks1() != null) {
        println("set request!")
        Get("http://jsonplaceholder.typicode.com/users/1")
      } else {
        println("no request to set!")
        null
      }
    }

    Rx {
      println("re-evaluating map. Something changed!")
      val userResponse = response.apply()
      println(s"the user response is $userResponse")
      val user = if (userResponse != null) {
        val u = userResponse.split(" ")
        User(u(0), u(1), u(2))
      } else {
        User("(name)", "(email)", "(website)")
      }
      println(s"user to return = $user")

      Map(
        "dom" -> {
          Div(children = Seq(
            Button(className = "get-first", children = Seq(Text(if (request() == null) "Get first user" else "Getting first user"))),
            Button(className = "abort-load", children = Seq(Text(if (request() == null) "Do nothing..." else "Stop loading"))),
            Div(className = "user-details", children = Seq(
              H1(className = "user-name", children = Seq(Text(user.name))),
              Div(className = "user-email", children = Seq(Text(user.email))),
              A(className = "user-website", href = user.website, children = Seq(Text(user.website)))
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

case class User(name: String, email: String, website: String)
