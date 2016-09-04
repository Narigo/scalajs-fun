package com.campudus.scycle.http

sealed trait Response

case class TextResponse(url: String, body: String) extends Response
