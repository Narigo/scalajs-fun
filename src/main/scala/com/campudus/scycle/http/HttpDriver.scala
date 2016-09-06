package com.campudus.scycle.http

import com.campudus.scycle.Driver
import rx.{Ctx, Rx}

class HttpDriver(input: Rx[Request])(implicit ctx: Ctx.Owner) extends Driver {

  println(s"owner in HttpDriver = $ctx")

  val response: Rx[String] = Rx({
    println(s"owner in HttpDriver.response = $ctx")
    val request = input()
    if (request == null || request == NonRequest) {
      println(s"null request received")
      "(name) (email) (website)"
    } else {
      val response = request.url.split("/").last
      println(s"response of $request = $response")
      s"(name_$response) (email_$response) (website_$response)"
    }
  })(ctx)

  def getResponse(): Rx[String] = response
}
