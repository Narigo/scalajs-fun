package com.campudus.scycle.http

sealed trait Request {

  val id: String

  val url: String

}

case class Get(id: String, url: String) extends Request
