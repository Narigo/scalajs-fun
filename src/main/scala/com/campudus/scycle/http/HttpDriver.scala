package com.campudus.scycle.http

import com.campudus.scycle.Driver
import rx.{Ctx, Rx}

class HttpDriver(input: Rx[Request])(implicit ctx: Ctx.Owner) extends Driver {

  def getResponse(): Rx[String] = Rx {
    val request = input()
    if (request == null) {
      "(empty)"
    } else {
      val response = request.url
      println(s"response of $request = $response")
      response
    }
  }

}
