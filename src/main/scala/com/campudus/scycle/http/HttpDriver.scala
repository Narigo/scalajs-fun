package com.campudus.scycle.http

import com.campudus.scycle.Driver
import com.campudus.scycle.Scycle.side
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

class HttpDriver extends Driver[Request] {

  override def subscribe(requests: Observable[Request]): AnonymousSubscription = requests.subscribe(_ => null)

  val responses: Subject[User] = Subject()
  val lastResponse$: Observable[User] = responses.startWith(null)

  def requestUser(number: Double): Observable[User] = {
    work(Get(s"http://jsonplaceholder.typicode.com/users/$number")).map(side(responses.next))
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
