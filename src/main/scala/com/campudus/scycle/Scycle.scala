package com.campudus.scycle

import rxscalajs._

object Scycle {

  def run(
           mainFn: (collection.Map[String, Driver]) => Observable[collection.Map[String, _]],
           drivers: collection.Map[String, Observable[_] => Driver]
         ): Unit = {

    println("run method")

  }

}
