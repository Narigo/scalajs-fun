package com.campudus.scycle.http

import rxscalajs._

object HttpDriver {
  def apply(input: Observable[Request]): Observable[UserResponse] = input.flatMap({ r =>
    println("request in httpdriver")
    Observable
      .ajax(r.url)
      .map(p => {
        val user = p.response
        UserResponse(r.url, User(user.username.toString, user.email.toString, user.website.toString))
      })
  })
}
