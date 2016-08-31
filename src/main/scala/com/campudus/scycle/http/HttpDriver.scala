package com.campudus.scycle.http

import com.campudus.scycle.Driver
import rx.{Ctx, Rx, Var}

import scala.util.Random

class HttpDriver(input: Rx[Request])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing http driver...")
  val request = Rx {
    println("re-evaluating request due to changed input")
    Option(input())
  }

  val response = Rx {
    println("evaluating response due to changed request")
    val receivedRequest = request()
    println(s"the request received: $receivedRequest")
    println(s"received new request ${receivedRequest.getOrElse("none")}")
    s"(name_${Random.nextInt()}) (email_${Random.nextInt()}) (website_${Random.nextInt()})"
  }

  def getResponse()(implicit ctx: Ctx.Owner): Rx[String] = response

}
