package com.campudus.scycle.http

import com.campudus.scycle.{Driver, Mapper}
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

import scala.scalajs.js

class HttpDriver private extends Driver[Request] {

  override def subscribe(requests: Observable[Request]): AnonymousSubscription = {
    requests
      .map(req => Option(req).map(request))
      .flatMap(_.getOrElse(Observable.just(null)))
      .subscribe(responses.next _)
  }

  val responses: Subject[js.Dynamic] = Subject()

  def request(req: Request): Observable[js.Dynamic] = {
    Observable
      .ajax(req.url)
  }

}

object HttpDriver extends Mapper[HttpDriver, js.Dynamic] {

  case object Http

  type Key = Http.type

  def makeHttpDriver() = new HttpDriver

}
