package com.campudus.scycle.http

import rxscalajs.Observable

import scala.scalajs.js

case class ResponseObservable(request: Request, observable: Observable[Response])
  extends Observable[Response](observable.inner) {

  org.scalajs.dom.console.log("Created a ResponseObservable", request.url)
  observable.subscribe(res => {
    org.scalajs.dom.console.log("ResponseObservable.subscribe", request.url, "=>", res.response)
  })
}

case class Response(id: String, url: String, response: js.Dynamic)

case class User(name: String, email: String, website: String)
