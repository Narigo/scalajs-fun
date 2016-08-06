package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Hyperscript, HyperscriptElement}

import scala.collection.mutable.ListBuffer

object VirtualDom {

  def diff(a: Hyperscript, b: Hyperscript): List[Diff] = {
    naiveDiff(a, b, ListBuffer()).toList
  }

  def naiveDiff(a: Hyperscript, b: Hyperscript, currentPath: ListBuffer[Int] = ListBuffer()): ListBuffer[Diff] = {
    if (a != b) {
      (a, b) match {
        case (aElem: HyperscriptElement, bElem: HyperscriptElement) =>
          if (aElem.attrs.equals(bElem.attrs)) {
            val first = aElem.subElements.zipWithIndex.flatMap({
              case (elem, idx) =>
                naiveDiff(elem, bElem.subElements(idx), currentPath :+ idx)
            })
            val added: Seq[Insertion] = if (aElem.subElements.length < bElem.subElements.length) {
              bElem.subElements.zipWithIndex.dropWhile(_._2 < aElem.subElements.length).map({
                case (newNode, idx) => Insertion((currentPath :+ idx).toList, newNode)
              })
            } else Nil
            first ++: ListBuffer(added: _*)
          } else {
            ListBuffer(Replacement(currentPath.toList, bElem))
          }
        case _ => ListBuffer(Replacement(currentPath.toList, b))
      }
    } else {
      ListBuffer()
    }
  }

  type Path = List[Int]

  sealed trait Diff

  case class Replacement(selectChildren: Path, newNode: Hyperscript) extends Diff

  case class Insertion(selectInsertionNode: Path, newNode: Hyperscript) extends Diff

}


