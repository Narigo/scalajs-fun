package com.campudus.scycle.driver

import org.scalajs.dom
import rx.{Ctx, Rx}

class ConsoleDriver(input: Rx[String])(implicit ctx: Ctx.Owner) extends Driver {
  Rx {
    dom.console.log(input())
  }
}
