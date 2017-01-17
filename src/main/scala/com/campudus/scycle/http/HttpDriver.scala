package com.campudus.scycle.http

import com.campudus.scycle.Driver
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

class HttpDriver extends Driver[Request] {

  override def subscribe(requests: Observable[Request]): AnonymousSubscription = {
    println(s"subscribed to requests: $requests")
    requests
      .map(request => {
        println(s"got a request $request")
        request
      })
      .flatMap(request => {
        println(s"observed request: $request")
        if (request == null) {
          Observable.just(null)
        } else {
          work(request)
        }
      })
      .subscribe(user => {
        println(s"got a user: $user")
        responses.next(user)
      })
  }

  val responses: Subject[User] = Subject()
  val lastResponse$: Observable[User] = responses.startWith(null)

  def requestUser(number: Double): Request = {
    Get(s"http://jsonplaceholder.typicode.com/users/$number")
  }

  private def work(request: Request): Observable[User] = {
    println(s"starting request $request")
    Observable
      .ajax(request.url)
      .map(p => {
        println(s"got a response for request $request")
        val user = p.response
        User(user.username.toString, user.email.toString, user.website.toString)
      })
  }

}
