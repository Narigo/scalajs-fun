package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Div, H1, Span, Text}
import com.campudus.scycle.vdom.VirtualDom.{Insertion, Path, Replacement}
import org.scalajs.dom
import org.scalatest.FunSpec

class VirtualDomTest extends FunSpec {

  describe("scalajs test setup") {
    it("can tell if two divs are the same") {
      val container = dom.document.createElement("div")
      val div = dom.document.createElement("div")
      container.appendChild(div)
      dom.console.log("container", container)
      dom.console.log("container.firstChild", container.firstChild)
      assert(container == container)
      assert(div == container.firstChild)
      assert(div == container.firstChild.asInstanceOf[dom.Element])

      dom.console.log("container.firstElementChild", container.firstElementChild)
    }
  }

  describe("The virtual dom diffing algorithm") {

    describe("doing replacements") {
      it("detects the same element") {
        val myH1 = H1("hello")

        assert(VirtualDom.diff(myH1, myH1) === Nil)
      }

      it("replaces two different elements") {
        val myH1 = H1("hello")
        val mySpan = Span("bye")

        assert(VirtualDom.diff(myH1, mySpan) === List(Replacement(Path(), mySpan)))
      }

      it("replaces same element with different attributes") {
        val myH1a = H1("hello")
        val myH1b = H1("bye")

        assert(VirtualDom.diff(myH1a, myH1b) === List(Replacement(Path(), myH1b)))
      }

      it("only replaces a changed text if the tags above are the same") {
        val firstDiv = Div(children = Seq(Text("hello")))
        val secondDiv = Div(children = Seq(Text("bye")))

        assert(VirtualDom.diff(firstDiv, secondDiv) === List(Replacement(Path(0), Text("bye"))))
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
          Replacement(Path(0), Text("secondA")),
          Replacement(Path(1), Text("secondB"))
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
          Replacement(Path(0), Text("secondA")),
          Replacement(Path(2), Text("secondC"))
        ))
      }

      it("can switch two elements by replacing them") {
        val before = Div(children = Seq(
          Text("a"),
          Text("b"),
          Text("c")
        ))
        val after = Div(children = Seq(
          Text("a"),
          Text("c"),
          Text("b")
        ))
        assert(VirtualDom.diff(before, after) === List(
          Replacement(Path(1), Text("c")),
          Replacement(Path(2), Text("b"))
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
          Replacement(Path(1, 0), Text("secondChildB1")),
          Replacement(Path(1, 2), Text("secondChildB3")),
          Replacement(Path(2), Text("secondC"))
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
          Replacement(Path(1), Div(children = Seq(
            Text("secondChildB1"),
            Text("secondChildB2"),
            Div(children = Seq(Text("secondChildB3a")))
          )))
        ))
      }
    }

    describe("doing insertions") {

      it("adds new elements") {
        val before = Div(children = Seq(
          Text("a")
        ))
        val after = Div(children = Seq(
          Text("a"),
          Text("b")
        ))
        assert(VirtualDom.diff(before, after) === List(
          Insertion(Path(1), Text("b"))
        ))
      }

      it("adds multiple new elements") {
        val before = Div(children = Seq(
          Text("a")
        ))
        val after = Div(children = Seq(
          Text("a"),
          Text("b"),
          Div(children = Seq(Text("c")))
        ))
        assert(VirtualDom.diff(before, after) === List(
          Insertion(Path(1), Text("b")),
          Insertion(Path(2), Div(children = Seq(Text("c"))))
        ))
      }

      it("can add elements before others") {
        val before = Div(children = Seq(
          Text("a")
        ))
        val after = Div(children = Seq(
          Text("b"),
          Text("a")
        ))
        assert(VirtualDom.diff(before, after) === List(
          Insertion(Path(0), Text("b"))
        ))
      }

      it("can add elements before others, not only at beginning") {
        val before = Div(children = Seq(
          Text("a"),
          Text("b")
        ))
        val after = Div(children = Seq(
          Text("a"),
          Text("c"),
          Text("b")
        ))
        assert(VirtualDom.diff(before, after) === List(
          Insertion(Path(1), Text("c"))
        ))
      }

      it("can add multiple elements with one before others") {
        val before = Div(children = Seq(
          Text("a"),
          Text("b")
        ))
        val after = Div(children = Seq(
          Text("c"),
          Text("a"),
          Text("b"),
          Text("d")
        ))
        assert(VirtualDom.diff(before, after) === List(
          Insertion(Path(0), Text("c")),
          Insertion(Path(3), Text("d"))
        ))
      }

      it("can add subtrees") {
        val before = Div(children = Seq(
          Text("a"),
          Text("b")
        ))
        val after = Div(children = Seq(
          Text("a"),
          Text("b"),
          Div(children = Seq(Text("c")))
        ))
        assert(VirtualDom.diff(before, after) === List(
          Insertion(Path(2), Div(children = Seq(Text("c"))))
        ))
      }

      it("can add subtrees before others") {
        val before = Div(children = Seq(
          Text("a"),
          Text("b")
        ))
        val after = Div(children = Seq(
          Text("a"),
          Div(children = Seq(Text("c"))),
          Text("b")
        ))
        assert(VirtualDom.diff(before, after) === List(
          Insertion(Path(1), Div(children = Seq(Text("c"))))
        ))
      }

      it("does not incorrectly optimize") {
        val before = Div(children = Seq(
          Text("a"),
          Text("b")
        ))
        val after = Div(children = Seq(
          Text("b"),
          Text("a"),
          Text("c")
        ))
        assert(VirtualDom.diff(before, after) === List(
          Replacement(Path(0), Text("b")),
          Replacement(Path(1), Text("a")),
          Insertion(Path(2), Text("c"))
        ))
      }
    }
  }

  describe("virtual dom") {
    it("can map real div elements to virtual dom div elements") {
      val realDiv = dom.document.createElement("div")
      realDiv.setAttribute("class", "hello")
      val virtualDiv = Div("hello")
      val result = VirtualDom(realDiv)

      assert(result === virtualDiv)
    }
  }

  describe("replacements") {

    it("empty replacement won't change elements") {
      val div = dom.document.createElement("div")
      val sub = dom.document.createElement("div")
      div.appendChild(sub)
      assert(sub == div.firstChild)
      VirtualDom.update(div, List())
      assert(sub == div.firstChild)
    }

  }

}
