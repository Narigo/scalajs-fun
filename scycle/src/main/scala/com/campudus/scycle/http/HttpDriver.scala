package com.campudus.scycle.http

import com.campudus.scycle.Driver
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

class HttpDriver private extends Driver[Request] {

  override def subscribe(requests: Observable[Request]): AnonymousSubscription = {
    requests
      .flatMap(request => {
        if (request == null) {
          Observable.just(null)
        } else {
          work(request)
        }
      })
      .subscribe(responses.next _)
  }

  val responses: Subject[User] = Subject()
  val lastResponse$: Observable[User] = responses.startWith(null)

  def requestUser(number: Double): Request = {
    Get(s"http://jsonplaceholder.typicode.com/users/$number")
  }

  private def work(request: Request): Observable[User] = {
    Observable
      .ajax(request.url)
      .map(p => {
        val user = p.response
        User(user.username.toString, user.email.toString, user.website.toString)
      })
  }

}

object HttpDriver {

  def makeHttpDriver() = new HttpDriver

}
