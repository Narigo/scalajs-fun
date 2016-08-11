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

class HyperscriptElement(tagName: String, val subElements: Seq[Hyperscript]) extends Hyperscript {

  def attrs: Map[String, Option[String]] = Map.empty

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

trait ClassNameAttr extends HyperscriptElement {
  val className: String

  abstract override def attrs: Map[String, Option[String]] = super.attrs + ("class" -> Option(className))

}

case class H1(className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("h1", children) with ClassNameAttr

case class Span(className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("span", children) with ClassNameAttr

case class Div(className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("div", children) with ClassNameAttr

case class Label(className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("label", children) with ClassNameAttr

case class Hr(className: String = null) extends HyperscriptElement("hr", Seq.empty) with ClassNameAttr

case class Input(className: String = null, kind: String, value: String = "") extends HyperscriptElement("input", Seq.empty) with ClassNameAttr {

  override def attrs: Map[String, Option[String]] =
    super.attrs.+(
      "type" -> Option(kind),
      "value" -> Option(value)
    )

}
