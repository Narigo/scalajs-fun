package com.campudus.scycle.http

import rxscalajs._

object HttpDriver {
  def apply(input: Observable[Request]): Observable[TextResponse] = input.flatMap({ r =>
    println("request in httpdriver")
    Observable
      .ajax(r.url)
      .map(p => {
        val d = p._1
        TextResponse(r.url, "a b c")
      })
  })
}
