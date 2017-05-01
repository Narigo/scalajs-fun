package com.campudus.scycle.http

import com.campudus.scycle.{Driver, Mapper}
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

import scala.scalajs.js

class HttpDriver private extends Driver[Request] {

  override def subscribe(requests: Observable[Request]): AnonymousSubscription = {
    requests
      .map(req => Option(req).map(work))
      .flatMap(_.getOrElse(Observable.just(null)))
      .subscribe(responses.next _)
  }

  val responses: Subject[js.Dynamic] = Subject()
  val lastResponse$: Observable[js.Dynamic] = responses.startWith(null)

  def requestUser(number: Double): Request = {
    Get(s"http://jsonplaceholder.typicode.com/users/$number")
  }

  private def work(request: Request): Observable[js.Dynamic] = {
    Observable
      .ajax(request.url)
  }

}

object HttpDriver extends Mapper[HttpDriver, js.Dynamic] {

  case object Http

  type Key = Http.type

  def makeHttpDriver() = new HttpDriver

}
