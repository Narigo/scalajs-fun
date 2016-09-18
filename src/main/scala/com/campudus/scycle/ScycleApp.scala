package com.campudus.scycle

import com.campudus.scycle.dom._
import com.campudus.scycle.http._
import org.scalajs.dom.Event
import rxscalajs._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.util.Random

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    println("main export")

    Scycle.run(logic, drivers)
  }

  val drivers: Map[String, Observable[_] => Observable[_]] = Map(
    "dom" -> (obs => DomDriver.apply(obs.asInstanceOf[Observable[Hyperscript]])),
    "http" -> (obs => HttpDriver.apply(obs.asInstanceOf[Observable[Request]]))
  )

  def logic(drivers: (Map[String, Observable[_]])): Map[String, Observable[_]] = {
    println("called logic")
    val clicks$ = drivers("dom").asInstanceOf[Observable[Event]]
    val requests$ = drivers("http").asInstanceOf[Observable[Response]]

    Map(
      "dom" -> clicks$
        .zip(requests$.map({ response =>
          val strings = response.asInstanceOf[TextResponse].body.split(" ")
          User(strings(0), strings(1), strings(2))
        }))
        .map({ project =>
          val user = project._2
          Div(id = "app", children = Seq(
            Button(className = "get-first", children = Seq(Text(if (user == null) "Get first user" else "Getting first user"))),
            Button(className = "abort-load", children = Seq(Text(if (user == null) "Do nothing..." else "Stop loading"))),
            Div(className = "user-details", children = Seq(
              H1(className = "user-name", children = Seq(Text(s"${user.name}"))),
              Div(className = "user-email", children = Seq(Text(s"${user.email}"))),
              A(className = "user-website", href = s"${user.website}", children = Seq(Text(s"${user.website}")))
            ))
          ))
        }),
      "http" -> clicks$
        .map(ev => {
          Get(s"http://jsonplaceholder.typicode.com/users/${Math.abs(Random.nextInt()) % 10}")
        })
    )
  }
}

case class User(name: String, email: String, website: String)
