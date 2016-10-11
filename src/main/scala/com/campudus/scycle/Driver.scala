package com.campudus.scycle

import rxscalajs.Observable

class Driver[A](input: Observable[A]) {

  input.subscribe(_ => {})

}

object Driver {
  def apply[A](input: Observable[A]): Driver[A] = new Driver[A](input.map(i => {
    println(s"standard i=$i")
    i
  }))
}