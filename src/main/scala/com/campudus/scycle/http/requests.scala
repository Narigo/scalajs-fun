package com.campudus.scycle.http

sealed trait Request

case class Get(url: String)
