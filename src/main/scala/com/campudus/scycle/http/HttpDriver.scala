package com.campudus.scycle.http

import rxscalajs._

object HttpDriver extends (Observable[_] => Observable[UserResponse]) {

  def apply(input: Observable[_]): Observable[UserResponse] = work(input.asInstanceOf[Observable[Request]])

  private def work(input: Observable[Request]): Observable[UserResponse] = input.flatMap({ r =>
    println("request in httpdriver")
    Observable
      .ajax(r.url)
      .map(p => {
        val user = p.response
        UserResponse(r.url, User(user.username.toString, user.email.toString, user.website.toString))
      })
  })

}
