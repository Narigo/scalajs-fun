package com.campudus.scycle.http

import rxscalajs._

object HttpDriver {
  def apply(input: Observable[Request]): Observable[TextResponse] = input.flatMap({ r =>
    println("request in httpdriver")
    Observable
      .ajax(r.url)
      .map(p => {
        val user = p.response
        TextResponse(r.url, s"${user.username} ${user.email} ${user.website}")
      })
  })
}
