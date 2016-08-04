package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Div, H1, Span, Text}
import com.campudus.scycle.vdom.VirtualDom.Replacement
import org.scalatest.FunSpec

class VirtualDomTest extends FunSpec {

  describe("The virtual dom diffing algorithm") {

    describe("doing replacements") {
      it("detects the same element") {
        val myH1 = H1("hello")

        assert(VirtualDom.diff(myH1, myH1) === Nil)
      }

      it("replaces two different elements") {
        val myH1 = H1("hello")
        val mySpan = Span("bye")

        assert(VirtualDom.diff(myH1, mySpan) === List(Replacement(Nil, mySpan)))
      }

      it("replaces same element with different attributes") {
        val myH1a = H1("hello")
        val myH1b = H1("bye")

        assert(VirtualDom.diff(myH1a, myH1b) === List(Replacement(Nil, myH1b)))
      }

      it("only replaces a changed text if the tags above are the same") {
        val firstDiv = Div(children = Seq(Text("hello")))
        val secondDiv = Div(children = Seq(Text("bye")))

        assert(VirtualDom.diff(firstDiv, secondDiv) === List(Replacement(List(0), Text("bye"))))
      }

      it("can replace multiple child nodes") {
        val before = Div(children = Seq(
          Text("firstA"),
          Text("firstB")
        ))
        val after = Div(children = Seq(
          Text("secondA"),
          Text("secondB")
        ))

        assert(VirtualDom.diff(before, after) === List(
          Replacement(List(0), Text("secondA")),
          Replacement(List(1), Text("secondB"))
        ))
      }

      it("replaces only changed child nodes") {
        val before = Div(children = Seq(
          Text("firstA"),
          Text("firstB"),
          Text("firstC")
        ))
        val after = Div(children = Seq(
          Text("secondA"),
          Text("firstB"),
          Text("secondC")
        ))

        assert(VirtualDom.diff(before, after) === List(
          Replacement(List(0), Text("secondA")),
          Replacement(List(2), Text("secondC"))
        ))
      }

      it("replaces child nodes recursively if necessary") {
        val before = Div(children = Seq(
          Text("firstA"),
          Div(children = Seq(
            Text("firstChildB1"),
            Text("firstChildB2"),
            Text("firstChildB3")
          )),
          Text("firstC")
        ))
        val after = Div(children = Seq(
          Text("firstA"),
          Div(children = Seq(
            Text("secondChildB1"),
            Text("firstChildB2"),
            Text("secondChildB3")
          )),
          Text("secondC")
        ))

        assert(VirtualDom.diff(before, after) === List(
          Replacement(List(1, 0), Text("secondChildB1")),
          Replacement(List(1, 2), Text("secondChildB3")),
          Replacement(List(2), Text("secondC"))
        ))
      }

      it("replaces changed nodes with whole trees") {
        val before = Div(children = Seq(
          Text("firstA"),
          Text("firstB"),
          Text("firstC")
        ))
        val after = Div(children = Seq(
          Text("firstA"),
          Div(children = Seq(
            Text("secondChildB1"),
            Text("secondChildB2"),
            Div(children = Seq(Text("secondChildB3a")))
          )),
          Text("firstC")
        ))

        assert(VirtualDom.diff(before, after) === List(
          Replacement(List(1), Div(children = Seq(
            Text("secondChildB1"),
            Text("secondChildB2"),
            Div(children = Seq(Text("secondChildB3a")))
          )))
        ))
      }
    }

  }

}