package com.campudus.scycle.http

import rxscalajs._

import scala.util.Random

object HttpDriver {
  def apply(input: Observable[Request]): Observable[_] = input.map({ r =>
    println("request in httpdriver")
    s"Request $r -> ${Random.nextInt}"
  })
}
