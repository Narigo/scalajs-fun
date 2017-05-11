package com.campudus.scycle.http

import com.campudus.scycle.{Driver, Mapper}
import rxscalajs._
import rxscalajs.subscription.AnonymousSubscription

class HttpDriver private extends Driver[Request] {

  private val responses$$: Subject[ResponseObservable] = Subject()

  override def subscribe(inputs: Observable[Request]): AnonymousSubscription = {
    inputs
      .map(request => {
        val obs = Observable.ajax(request.url).map(response => {
          org.scalajs.dom.console.log("Got a response!", response)
          Response(request.id, request.url, response)
        })

        ResponseObservable(
          request,
          obs
        )
      })
      .subscribe(response => {
        org.scalajs.dom.console.log("Pushing", response.request.url, "into response$$")
        responses$$.next(response)
      })
  }

  def filter(predicate: ResponseObservable => Boolean): Observable[ResponseObservable] = {
    org.scalajs.dom.console.log("Filtering ResponseObservable")
    val obs = responses$$.filter(predicate)
    //    obs.subscribe(sth => {
    //      org.scalajs.dom.console.log("Something went through the filter!", sth.request.url)
    //    })
    obs
  }

}

object HttpDriver extends Mapper[HttpDriver, Request] {

  case object Http

  override type Key = Http.type

  def makeHttpDriver() = new HttpDriver

}
