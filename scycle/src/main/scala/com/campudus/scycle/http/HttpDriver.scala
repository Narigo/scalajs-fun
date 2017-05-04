package com.campudus.scycle.http

import com.campudus.scycle.{Driver, Mapper}
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

import scala.scalajs.js

class HttpDriver private extends Driver[Request] {

  def request(req: Request): Observable[js.Dynamic] = Observable.ajax(req.url)

}

object HttpDriver extends Mapper[HttpDriver, js.Dynamic] {

  case object Http

  type Key = Http.type

  def makeHttpDriver() = new HttpDriver

}
