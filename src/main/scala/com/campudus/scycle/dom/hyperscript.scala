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

object Hyperscript {

  def domToHyperscript(element: dom.Element): HyperscriptElement = {
    val applyFn = element.tagName.toLowerCase match {
      case "div" => Div.apply _
      case "h1" => H1.apply _
      case "span" => Span.apply _
      case "label" => Label.apply _
    }

    val mappedChildren = for {
      i <- 0 until element.children.length
    } yield domToHyperscript(element.children(i))

    applyFn(element.getAttribute("id"), element.getAttribute("class"), mappedChildren)
  }

}

object HyperscriptElement {

  def unapply(node: dom.Node): Option[_ <: Hyperscript] = node match {
    case element: dom.Element =>
      val simple = (element.tagName.toLowerCase match {
        case "div" => Some(Div.apply _)
        case "h1" => Some(H1.apply _)
        case "span" => Some(Span.apply _)
        case "label" => Some(Label.apply _)
        case _ => None
      }).map { applyFn =>
        val parent: Seq[Hyperscript] => HyperscriptElement = applyFn(element.getAttribute("id"), element.getAttribute("class"), _)
        val children = for {
          i <- 0 until element.childNodes.length
          children <- unapply(element.childNodes(i)).toSeq
        } yield children
        parent(children)
      }
      if (simple.isDefined) simple
      else {
        element.tagName.toLowerCase match {
          case "input" => Some(Input(element.getAttribute("class"), element.getAttribute("type"), element.getAttribute("value")))
          case "hr" => Some(Hr(element.getAttribute("class")))
          case _ => None
        }
      }
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

case class H1(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("h1", children) with IdAttr with ClassNameAttr

case class Span(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("span", children) with IdAttr with ClassNameAttr

case class Div(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("div", children) with IdAttr with ClassNameAttr

case class Label(id: String = null, className: String = null, children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("label", children) with IdAttr with ClassNameAttr

case class Hr(className: String = null, final val children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("hr", children) with ClassNameAttr

case class Input(className: String = null, kind: String = null, value: String = "", final val children: Seq[Hyperscript] = Seq.empty) extends HyperscriptElement("input", children) with ClassNameAttr {

  override def attrs: Map[String, Option[String]] =
    super.attrs.+(
      "type" -> Option(kind),
      "value" -> Option(value)
    )

}
