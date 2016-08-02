package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{H1, Span}
import org.scalatest.FlatSpec

class VirtualDomTest extends FlatSpec {


  "The virtual dom diffing algorithm" should "detect the same element" in {
    val myH1 = H1("hello")

    assert(VirtualDom.diff(myH1, myH1) === None)
  }

  it should "tell us to replace two different elements" in {
    val myH1 = H1("hello")
    val mySpan = Span("bye")

    assert(VirtualDom.diff(myH1, mySpan) === Some(mySpan))
  }

}
