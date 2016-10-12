package com.campudus.scycle

import rxscalajs.Observable

class Driver[A](input: Observable[A]) {

  println(s"create new Driver with $input")

  input.subscribe({ a =>
    println(s"got an A in Driver[A]=$a")
  })

  override def toString: String = s"Driver:$input"

}

object Driver {
  def apply[A](input: Observable[A]): Driver[A] = new Driver[A](input.map(i => {
    println(s"standard i=$i")
    i
  }))
}
