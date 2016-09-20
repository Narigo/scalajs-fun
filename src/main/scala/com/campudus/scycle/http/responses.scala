package com.campudus.scycle.http

sealed trait Response

case class TextResponse(url: String, body: String) extends Response

case class UserResponse(url: String, user: User) extends Response

case class User(name: String, email: String, website: String)
