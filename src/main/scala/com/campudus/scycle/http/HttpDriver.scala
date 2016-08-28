package com.campudus.scycle.http

import com.campudus.scycle.Driver
import rx.{Ctx, Rx, Var}

class HttpDriver(input: Rx[Option[Request]])(implicit ctx: Ctx.Owner) extends Driver {

  println("initializing http driver...")

  Rx {
    input.triggerLater {
      val request = input()
      println(s"new request received: $request")
    }
  }

  def responses()(implicit ctx: Ctx.Owner): Rx[Response] = {
    val responseEvent = Var[Response](null)

    responseEvent
  }
}
