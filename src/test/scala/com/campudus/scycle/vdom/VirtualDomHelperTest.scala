package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Div, H1, Span, Text}
import com.campudus.scycle.vdom.VirtualDom.Path
import org.scalatest.FunSpec

class VirtualDomHelperTest extends FunSpec {

  describe("getElement") {

    it("finds the correct element") {
      val elem = Div(children = Seq(
        Text("a"),
        Text("b")
      ))

      assert(VirtualDom.getElem(elem, Path(0)) === Text("a"))
      assert(VirtualDom.getElem(elem, Path(1)) === Text("b"))
    }

    it("finds the correct element recursively") {
      val elem = Div(children = Seq(
        Span(children = Seq(
          Text("a1"),
          Text("a2")
        )),
        Span(children = Seq(
          Text("b1"),
          Text("b2")
        ))
      ))

      assert(VirtualDom.getElem(elem, Path(0, 1)) === Text("a2"))
      assert(VirtualDom.getElem(elem, Path(1, 0)) === Text("b1"))
    }

    it("finds the parent element if empty path given") {
      val elem = Div(children = Seq(
        Span(children = Seq(
          Text("a1"),
          Text("a2")
        )),
        Span(children = Seq(
          Text("b1"),
          Text("b2")
        ))
      ))

      assert(VirtualDom.getElem(elem, Path()) === elem)
    }

    it("throws if the path is not defined") {
      val elem = Div(children = Seq(
        Span(children = Seq(
          Text("a1"),
          Text("a2")
        ))
      ))

      assert(VirtualDom.getElem(elem, Path(0, 1)) === Text("a2"))
      assertThrows[NoSuchElementException](VirtualDom.getElem(elem, Path(1, 0)))
    }

  }

}
