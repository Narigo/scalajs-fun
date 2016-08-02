package com.campudus.scycle.vdom

import com.campudus.scycle.dom.Hyperscript

object VirtualDom {

  def diff(a: Hyperscript, b : Hyperscript): Option[Hyperscript] = {
    if (a != b) Some(b)
    else None
  }

}
