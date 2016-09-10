package com.campudus.scycle

import rxscalajs._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ScycleApp extends JSApp {

  @JSExport
  def main(): Unit = {
    println("main export")

    val o = Observable
      .interval(200)
      .take(5)
    o.subscribe(n => println(s"n=$n"))

    println(s"having the o $o")

  }

}
