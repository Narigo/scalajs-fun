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

class HyperscriptElement(tagName: String, className: String, val subElements: Seq[Hyperscript]) extends Hyperscript {

  def attrs: Map[String, Option[String]] = Map("class" -> Option(className))

  override def toElement: dom.Element = {
    val element = dom.document.createElement(tagName)
    for {
      (key, value) <- attrs
    } {
      value.foreach(element.setAttribute(key, _))
    }
    subElements.foreach { child =>
      element.appendChild(child.toElement)
    }
    element

  }

}

case class H1(className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("h1", className, children)

case class Span(className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("span", className, children)

case class Div(className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("div", className, children)

case class Label(className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("label", className, children)

case class Hr(className: String = null) extends HyperscriptElement("hr", className, Seq.empty)

case class Input(className: String = null, kind: String, value: String = "") extends HyperscriptElement("input", className, Seq.empty) {

  override def attrs: Map[String, Option[String]] = Map(
    "class" -> Option(className),
    "type" -> Option(kind),
    "value" -> Option(value)
  )

}
