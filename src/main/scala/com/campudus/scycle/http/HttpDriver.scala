package com.campudus.scycle.http

import com.campudus.scycle.Driver
import rx.{Ctx, Rx}

class HttpDriver(input: Rx[Request])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing http driver...")
  val request = Rx {
    println("evaluate val request in HttpDriver")
    val req = input()
    if (req == null) {
      ""
    } else {
      req.url
    }
  }

  val response = Rx {
    println("evaluate val response in HttpDriver")
    val receivedRequest = request()
    println(s"evaluate val response in HttpDriver 2 $receivedRequest")
    val num = receivedRequest
    println("evaluate val response in HttpDriver 2b")
    val number = num.last
    println("evaluate val response in HttpDriver 3")
    val result = s"(name_$number) (email_$number) (website_$number)"
    println("evaluate val response in HttpDriver 4")
    result
  }

  def getResponse()(implicit ctx: Ctx.Owner): Rx[String] = Rx {
    println("evaluate def getResponse in HttpDriver")
    response()
  }

}
