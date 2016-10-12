package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Div, Span, Text}
import com.campudus.scycle.vdom.VirtualDom.{Insertion, Path, Replacement}
import org.scalatest.FunSpec

import scala.collection.mutable.ListBuffer

class VirtualDomHelperTest extends FunSpec {

  describe("firstReplacementsCanBeInsertions") {

    it("tells if single insert can be moved at front of list") {
      val elem = Div(children = Seq(
        Text("a")
      ))
      val list = ListBuffer(Replacement(ListBuffer(0), Text("b")), Insertion(ListBuffer(1), Text("a")))
      assert(VirtualDom.firstReplacementsCanBeInsertions(elem, list) === true)
    }

    it("tells if insert cant be moved at front of list") {
      val elem = Div(children = Seq(
        Text("a")
      ))
      val list = ListBuffer(Replacement(ListBuffer(0), Text("b")), Insertion(ListBuffer(1), Text("c")))
      assert(VirtualDom.firstReplacementsCanBeInsertions(elem, list) === false)
    }

    it("tells that no insert can be made when no insert is present in list") {
      val elem = Div(children = Seq(
        Text("a")
      ))
      val list = ListBuffer(Replacement(ListBuffer(0), Text("b")))
      assert(VirtualDom.firstReplacementsCanBeInsertions(elem, list) === false)
    }

    it("tells that no insert can be made when no insert is present in longer list") {
      val elem = Div(children = Seq(
        Text("a"),
        Text("b"),
        Text("c")
      ))
      val list = ListBuffer(
        Replacement(ListBuffer(0), Text("b")),
        Replacement(ListBuffer(1), Text("c"))
      )
      assert(VirtualDom.firstReplacementsCanBeInsertions(elem, list) === false)
    }

    it("tells if insert can be moved at front of list if list is longer") {
      val elem = Div(children = Seq(
        Text("a"),
        Text("b"),
        Text("c")
      ))
      val list = ListBuffer(
        Replacement(ListBuffer(0), Text("d")),
        Replacement(ListBuffer(1), Text("a")),
        Replacement(ListBuffer(2), Text("b")),
        Insertion(ListBuffer(3), Text("c"))
      )
      assert(VirtualDom.firstReplacementsCanBeInsertions(elem, list) === true)
    }

    it("tells if insert cant be moved to front of list if list is longer") {
      val elem = Div(children = Seq(
        Text("a"),
        Text("b"),
        Text("c")
      ))
      val list = ListBuffer(
        Replacement(ListBuffer(0), Text("c")),
        Replacement(ListBuffer(1), Text("a")),
        Replacement(ListBuffer(2), Text("b")),
        Insertion(ListBuffer(3), Text("d"))
      )
      assert(VirtualDom.firstReplacementsCanBeInsertions(elem, list) === false)
    }

    it("tells if multiple inserts can be moved to front of list") {
      val elem = Div(children = Seq(
        Text("a")
      ))
      val list = ListBuffer(
        Replacement(ListBuffer(0), Text("b")),
        Insertion(ListBuffer(1), Text("c")),
        Insertion(ListBuffer(2), Text("a"))
      )
      assert(VirtualDom.firstReplacementsCanBeInsertions(elem, list) === true)
    }

  }

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
