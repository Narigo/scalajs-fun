package com.campudus.scycle.http

sealed trait Request {
  val url: String
}

case class Get(url: String) extends Request

case object NonRequest extends Request {
  val url: String = ""
}
