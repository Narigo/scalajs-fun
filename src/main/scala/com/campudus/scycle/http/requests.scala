package com.campudus.scycle.http

sealed trait Request {
  val url: String
}

case class Get(url: String)
