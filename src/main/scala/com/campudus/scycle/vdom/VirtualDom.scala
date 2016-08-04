package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Hyperscript, HyperscriptElement}

import scala.collection.mutable.ListBuffer

object VirtualDom {

  def diff(a: Hyperscript, b: Hyperscript, currentPath: ListBuffer[Int] = ListBuffer()): List[Replacement] = {
    if (a != b) {
      (a, b) match {
        case (aElem: HyperscriptElement, bElem: HyperscriptElement) =>
          if (aElem.attrs.equals(bElem.attrs)) {
            aElem.subElements.zipWithIndex.flatMap({
              case (elem, idx) =>
                diff(elem, bElem.subElements(idx), currentPath :+ idx)
            }).toList
          } else {
            List(Replacement(currentPath.toList, bElem))
          }
        case _ => List(Replacement(currentPath.toList, b))
      }
    } else {
      Nil
    }
  }

  type Path = List[Int]

  case class Replacement(selectChildren: Path, newNode: Hyperscript)

}


