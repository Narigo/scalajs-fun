package com.campudus.scycle.vdom

import com.campudus.scycle.dom._
import com.campudus.scycle.vdom.VirtualDom.{Insertion, Path, Replacement}
import org.scalajs.dom
import org.scalatest.FunSpec

class VirtualDomTest extends FunSpec {

  describe("scalajs test setup") {
    it("can tell if two divs are the same") {
      val container = dom.document.createElement("div")
      val div = dom.document.createElement("div")
      container.appendChild(div)
      assert(container == container)
      assert(div == container.firstChild)
      assert(div == container.firstChild.asInstanceOf[dom.Element])
    }
  }

  describe("The virtual dom diffing algorithm") {

    it("can apply null values") {
      assert(VirtualDom(null) === null)
    }

    it("can apply all kinds of elements") {
      def check[T <: Hyperscript](tagName: String, kind: Class[T]): Unit = {
        val element = dom.document.createElement(tagName)
        assert(kind.isInstance(VirtualDom(element)))
      }

      check("div", classOf[Div])
      check("h1", classOf[H1])
      check("span", classOf[Span])
      check("hr", classOf[Hr])
      check("input", classOf[Input])
      check("label", classOf[Label])
      check("button", classOf[Button])
      check("p", classOf[P])
      check("a", classOf[A])
    }

    it("does not yield changes if nothing changes") {

      val first = Div(children = Seq(
        Button(className = "get-first", children = Seq(Text("something a 1"))),
        Button(className = "get-second", children = Seq(Text("something b 1"))),
        Div(className = "user-details", children = Seq(
          H1(className = "user-name", children = Seq(Text("(name)"))),
          Div(className = "user-email", children = Seq(Text("(email)"))),
          A(className = "user-website", href = "https://example.com", children = Seq(Text("(website)")))
        ))
      ))
      val second = Div(children = Seq(
        Button(className = "get-first", children = Seq(Text("something a 2"))),
        Button(className = "get-second", children = Seq(Text("something b 2"))),
        Div(className = "user-details", children = Seq(
          H1(className = "user-name", children = Seq(Text("(name)"))),
          Div(className = "user-email", children = Seq(Text("(email)"))),
          A(className = "user-website", href = "https://example.com", children = Seq(Text("(website)")))
        ))
      ))

      val diffs = VirtualDom.diff(first, second)
      assert(diffs.length === 2)
      assert(diffs.forall(diff => diff.isInstanceOf[Replacement]))
    }

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

      it("can handle null elements") {
        val diffs = VirtualDom.diff(null, Div())
        assert(diffs.length === 1)
        assert(diffs.head.isInstanceOf[Insertion])
      }

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
      val virtualDiv = Div(className = "hello")
      val result = VirtualDom(realDiv)

      assert(result === virtualDiv)
    }

    it("can map real div elements with child elements to virtual dom div elements") {
      val containerDiv = dom.document.createElement("div")
      val childDiv = dom.document.createElement("div")
      childDiv.setAttribute("class", "child")
      containerDiv.setAttribute("class", "container")
      containerDiv.appendChild(childDiv)

      val virtualDiv = Div(className = "container", children = Seq(Div(className = "child")))
      val result = VirtualDom(containerDiv)

      assert(result === virtualDiv)
    }
  }

  describe("removals") {

    it("can remove child elements") {
      val containerDiv = dom.document.createElement("div")
      val childDiv = dom.document.createElement("div")
      childDiv.setAttribute("class", "child")
      containerDiv.setAttribute("class", "container")
      containerDiv.appendChild(childDiv)

      val virtualDiv = Div(className = "container", children = Seq())
      val diff = VirtualDom.diff(VirtualDom(containerDiv), virtualDiv)
      VirtualDom.update(containerDiv, diff)

      assert(containerDiv.childNodes.length === 0)
    }

  }

  describe("replacements") {

    it("insertions in root are possible") {
      val container = dom.document.createElement("div")
      val test = Div(className = "test")
      val diffs = VirtualDom.diff(null, test)
      assert(container.children.length === 0)
      VirtualDom.update(container, diffs)
      assert(container.children.length > 0)
    }

    it("remove in root is possible") {
      val container = dom.document.createElement("div")
      val test = dom.document.createElement("div")
      test.setAttribute("class", "test")
      container.appendChild(test)
      val diffs = VirtualDom.diff(VirtualDom(container), null)
      assert(container.children.length > 0)
      VirtualDom.update(container, diffs)
      assert(container.children.length === 0)
    }

    it("empty replacement won't change elements") {
      val div = dom.document.createElement("div")
      val sub = dom.document.createElement("div")
      div.appendChild(sub)
      assert(sub == div.firstChild)
      VirtualDom.update(div, List())
      assert(sub == div.firstChild)
    }

    it("will reuse the element and just replace its attributes") {
      val container = dom.document.createElement("div")
      val realDiv = dom.document.createElement("div")
      realDiv.setAttribute("class", "first")
      container.appendChild(realDiv)

      val firstDiv = VirtualDom(realDiv)
      val secondDiv = Div(className = "second")
      val diff = VirtualDom.diff(firstDiv, secondDiv)
      VirtualDom.update(container, diff)

      val resultDiv = container.firstChild

      assert(resultDiv == realDiv)
    }

    it("can insert new elements and not change existing ones") {
      val container = dom.document.createElement("div")
      val realDivA = dom.document.createElement("div")
      realDivA.setAttribute("class", "first")
      container.appendChild(realDivA)

      val containerBefore = VirtualDom(container)
      val containerAfter = Div(children = Seq(
        Div(className = "first"),
        Div(className = "second")
      ))
      val diff = VirtualDom.diff(containerBefore, containerAfter)

      VirtualDom.update(container, diff)

      val resultFirstDiv = container.firstChild
      val resultSecondDiv = container.childNodes(1).asInstanceOf[dom.Element]

      assert(resultFirstDiv == realDivA)
      assert(resultSecondDiv.getAttribute("class") === "second")
    }

    it("can replace input values") {
      val container = dom.document.createElement("div")
      val firstInput = dom.document.createElement("input")
      firstInput.setAttribute("value", "hello")
      container.appendChild(firstInput)

      val containerBefore = VirtualDom(container)
      val containerAfter = Div(children = Seq(
        Input(options = List("value" -> "bye"))
      ))
      val diff = VirtualDom.diff(containerBefore, containerAfter)

      VirtualDom.update(container, diff)

      val resultA = container.firstElementChild

      assert(resultA == firstInput)
      assert(resultA.getAttribute("value") === "bye")
    }

    it("can replace any hyperscript element") {
      val container = dom.document.createElement("div")
      val h1 = dom.document.createElement("h1")
      val span = dom.document.createElement("span")
      h1.setAttribute("class", "testh1")
      span.setAttribute("class", "testspan")
      container.appendChild(h1)
      container.appendChild(span)

      val containerBefore = VirtualDom(container)
      val containerAfter = Div(children = Seq(
        H1(className = "testafterh1"),
        Span(className = "testafterspan")
      ))
      val diff = VirtualDom.diff(containerBefore, containerAfter)

      VirtualDom.update(container, diff)

      val resultA = container.firstElementChild
      val resultB = container.childNodes(1).asInstanceOf[dom.Element]

      assert(resultA == h1)
      assert(resultA.getAttribute("class") === "testafterh1")
      assert(resultB == span)
      assert(resultB.getAttribute("class") === "testafterspan")
    }

    it("can replace Text nodes") {
      val div = dom.document.createElement("div")
      div.appendChild(dom.document.createTextNode("before"))
      assert(div.textContent === "before")
      val before = VirtualDom(div)
      assert(before == Div(children = Seq(Text("before"))))
      val after = Div(children = Seq(Text("after")))
      val diffs = VirtualDom.diff(before, after)
      VirtualDom.update(div, diffs)
      assert(div.textContent === "after")
    }

    it("can replace nested elements with not so nested elements") {
      val div = dom.document.createElement("div")
      div.setAttribute("id", "div")
      val span = dom.document.createElement("span")
      span.setAttribute("id", "span")
      val deep = dom.document.createElement("span")
      deep.setAttribute("id", "deep")
      span.appendChild(deep)
      div.appendChild(span)
      val before = VirtualDom(div)
      assert(before == Div(id = "div", children = Seq(Span(id = "span", children = Seq(Span(id = "deep"))))))
      val after = Div(id = "div", children = Seq(Span(id = "shallow")))
      val diffs = VirtualDom.diff(before, after)
      VirtualDom.update(div, diffs)
      assert(div.firstElementChild.getAttribute("id") === "shallow")
      assert(div.childNodes.length === 1)
      assert(div.firstElementChild.childNodes.length === 0)
    }

    it("can replace elements with text nodes") {
      val div = dom.document.createElement("div")
      val span = dom.document.createElement("span")
      span.appendChild(dom.document.createTextNode("before"))
      div.appendChild(span)
      assert(div.textContent === "before")
      val before = VirtualDom(div)
      assert(before == Div(children = Seq(Span(children = Seq(Text("before"))))))
      val after = Div(children = Seq(Text("after")))
      val diffs = VirtualDom.diff(before, after)
      assert(diffs == List(Replacement(Path(0), Text("after"))))
      VirtualDom.update(div, diffs)
      assert(div.textContent === "after")
    }

  }

}
