package com.campudus.scycle.dom

import org.scalajs.dom

sealed trait Hyperscript {
  def toNode: dom.Node
}

case class Text(text: String) extends Hyperscript {
  override def toNode: dom.Node = {
    dom.document.createTextNode(text)
  }
}

object Hyperscript

object HyperscriptElement {

  def unapply(node: dom.Node): Option[_ <: Hyperscript] = node match {
    case element: dom.Element =>
      type Children = Seq[Hyperscript] => HyperscriptElement
      (element.tagName.toLowerCase match {
        case "div" => Some(Div.curried(element.getAttribute("id"))(element.getAttribute("class")))
        case "h1" => Some(H1.curried(element.getAttribute("id"))(element.getAttribute("class")))
        case "span" => Some(Span.curried(element.getAttribute("id"))(element.getAttribute("class")))
        case "label" => Some(Label.curried(element.getAttribute("id"))(element.getAttribute("class")))
        case "button" => Some(Button.curried(element.getAttribute("id"))(element.getAttribute("class")))
        case "p" => Some(P.curried(element.getAttribute("id"))(element.getAttribute("class")))
        case "a" => Some(A.curried(element.getAttribute("id"))(element.getAttribute("class"))(element.getAttribute("href")))
        case _ => None
      }).map({ applyFn =>
        val parent: Seq[Hyperscript] => HyperscriptElement =
          applyFn(_)
        val children = for {
          i <- 0 until element.childNodes.length
          children <- unapply(element.childNodes(i)).toSeq
        } yield children
        parent(children)
      }).orElse({
        element.tagName.toLowerCase match {
          case "input" => Some(Input(element.getAttribute("class"), element.getAttribute("type"), element.getAttribute("value")))
          case "hr" => Some(Hr(element.getAttribute("class")))
          case _ => None
        }
      })
    case _ => Some(Text(node.textContent))
  }

}

abstract class HyperscriptElement(val tagName: String, val subElements: Seq[Hyperscript]) extends Hyperscript {

  def attrs: Map[String, Option[String]] = Map.empty

  override def toNode: dom.Element = {
    val element = dom.document.createElement(tagName)
    for {
      (key, value) <- attrs
    } {
      value.foreach(element.setAttribute(key, _))
    }
    subElements.foreach { child =>
      element.appendChild(child.toNode)
    }
    element

  }

}

trait IdAttr extends HyperscriptElement {
  val id: String

  abstract override def attrs: Map[String, Option[String]] = super.attrs + ("id" -> Option(id))
}

trait ClassNameAttr extends HyperscriptElement {
  val className: String

  abstract override def attrs: Map[String, Option[String]] = super.attrs + ("class" -> Option(className))
}

trait HrefAttr extends HyperscriptElement {
  val href: String

  abstract override def attrs: Map[String, Option[String]] = super.attrs + ("href" -> Option(href))
}

case class A(id: String = null, className: String = null, href: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("a", children) with IdAttr with ClassNameAttr with HrefAttr

case class H1(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("h1", children) with IdAttr with ClassNameAttr

case class Span(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("span", children) with IdAttr with ClassNameAttr

case class Div(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("div", children) with IdAttr with ClassNameAttr

case class P(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("p", children) with IdAttr with ClassNameAttr

case class Button(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("button", children) with IdAttr with ClassNameAttr

case class Label(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("label", children) with IdAttr with ClassNameAttr

case class Hr(className: String = null, final val children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("hr", children) with ClassNameAttr

case class Input(className: String = null, kind: String = null, value: String = "", final val children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("input", children) with ClassNameAttr {

  override def attrs: Map[String, Option[String]] =
    super.attrs.+(
      "type" -> Option(kind),
      "value" -> Option(value)
    )

}
