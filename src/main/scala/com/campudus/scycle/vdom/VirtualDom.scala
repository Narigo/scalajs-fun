package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Hyperscript, HyperscriptElement}

import scala.collection.mutable.ListBuffer

object VirtualDom {

  def diff(a: Hyperscript, b: Hyperscript, currentPath: ListBuffer[Int] = ListBuffer()): List[Diff] = {
    if (a != b) {
      (a, b) match {
        case (aElem: HyperscriptElement, bElem: HyperscriptElement) =>
          if (aElem.attrs.equals(bElem.attrs)) {
            val first = aElem.subElements.zipWithIndex.flatMap({
              case (elem, idx) =>
                diff(elem, bElem.subElements(idx), currentPath :+ idx)
            }).toList
            val added = if (aElem.subElements.length < bElem.subElements.length) {
              bElem.subElements.zipWithIndex.dropWhile(_._2 < aElem.subElements.length).map({
                case (newNode, idx) => Insertion((currentPath :+ idx).toList, newNode)
              }).toList
            } else Nil
            first ::: added
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

  sealed trait Diff

  case class Replacement(selectChildren: Path, newNode: Hyperscript) extends Diff

  case class Insertion(selectInsertionNode: Path, newNode: Hyperscript) extends Diff

}


