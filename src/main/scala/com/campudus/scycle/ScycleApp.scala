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
        .zip(requests$.map(r => r.asInstanceOf[UserResponse].user))
        .startWith((null, null))
        .map({ project =>
          val user = project._2
          Div(id = "app", children = Seq(
            Button(className = "get-first", children = Seq(Text("Get first user")))
          ) ++ (if (user != null) {
            Seq(
              Div(className = "user-details", children = Seq(
                H1(className = "user-name", children = Seq(Text(s"${user.name}"))),
                Div(className = "user-email", children = Seq(Text(s"${user.email}"))),
                A(className = "user-website", href = s"${user.website}", children = Seq(Text(s"${user.website}")))
              ))
            )
          } else Seq()))
        }),
      "http" -> clicks$
        .map(ev => {
          Get(s"http://jsonplaceholder.typicode.com/users/1")
        })
    )
  }
}
