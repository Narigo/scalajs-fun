package com.campudus.scycle.http

import com.campudus.scycle.Driver
import rxscalajs._

class HttpDriver(input: Observable[Request]) extends Driver {

}

object HttpDriver {
  def apply(input: Observable[Request]): HttpDriver = new HttpDriver(input)
}
