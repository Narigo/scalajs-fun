package com.campudus.scycle.http

import rxscalajs._

object HttpDriver {
  def apply(input: Observable[Request]): Observable[_] = Observable.just(null)
}
