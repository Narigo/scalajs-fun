package com.campudus.scycle

import rxscalajs.{Observable, Observer}

import scala.scalajs.js.Any

class Driver[A](input: Observable[A], output: Observer[A]) extends Observable[A](input.inner) with Observer[A] {

  println(s"create new Driver with $input")

  input.subscribe(output)

  override def toString: String = s"Driver:$input"

  override def next(t: A): Unit = output.next(t)
  override def error(err: Any): Unit = output.error(err)
  override def complete(): Unit = output.complete()
}
