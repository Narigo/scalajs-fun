package com.campudus.scycle.dom

import org.scalajs.dom

sealed trait Hyperscript {
  def toElement: dom.Element
}

case class Text(text: String) extends Hyperscript {
  override def toElement: dom.Element = {
    val span = dom.document.createElement("span")
    val textNode = dom.document.createTextNode(text)
    span.appendChild(textNode)
    span
  }
}

class HyperScriptElement(tagName: String, subElements: Seq[Hyperscript]) extends Hyperscript {

  override def toElement: dom.Element = {
    val element = dom.document.createElement(tagName)
    subElements.foreach { child =>
      element.appendChild(child.toElement)
    }
    element
  }
}

case class H1(children: Hyperscript*) extends HyperScriptElement("h1", children)

case class Span(children: Hyperscript*) extends HyperScriptElement("span", children)

case class Div(children: Hyperscript*) extends HyperScriptElement("div", children)

