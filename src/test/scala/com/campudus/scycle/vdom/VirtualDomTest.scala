package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Div, H1, Span, Text}
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalatest.FlatSpec

class VirtualDomTest extends FlatSpec {


  "The virtual dom diffing algorithm" should "detect the same element" in {
    val myH1 = H1("hello")

    assert(VirtualDom.diff(myH1, myH1) === Nil)
  }

  it should "replace two different elements" in {
    val myH1 = H1("hello")
    val mySpan = Span("bye")

    assert(VirtualDom.diff(myH1, mySpan) === List(Replacement(Nil, mySpan)))
  }

  it should "replace same element with different attributes" in {
    val myH1a = H1("hello")
    val myH1b = H1("bye")

    assert(VirtualDom.diff(myH1a, myH1b) === List(Replacement(Nil, myH1b)))
  }

  it should "only replace a changed text if the tags above are the same" in {
    val firstDiv = Div(children = Seq(Text("hello")))
    val secondDiv = Div(children = Seq(Text("bye")))

    assert(VirtualDom.diff(firstDiv, secondDiv) === List(Replacement(List(0), Text("bye"))))
  }

}
