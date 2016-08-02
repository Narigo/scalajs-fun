package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Hyperscript, HyperscriptElement}

object VirtualDom {

  def diff(a: Hyperscript, b: Hyperscript): List[Replacement] = {
    if (a != b) {
      (a, b) match {
        case (aElem: HyperscriptElement, bElem: HyperscriptElement) =>
          if (aElem.attrs.equals(bElem.attrs)) {
            List(Replacement(List(0), bElem.subElements.head))
          } else {
            println(aElem.attrs)
            println(bElem.attrs)
            List(Replacement(List(), bElem))
          }
        case _ => List(Replacement(Nil, b))
      }
    } else {
      Nil
    }
  }

  type Path = List[Int]

  case class Replacement(selectChildren: Path, newNode: Hyperscript)

}


