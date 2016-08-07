package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Hyperscript, HyperscriptElement}

import scala.collection.mutable.ListBuffer

object VirtualDom {

  def diff(a: Hyperscript, b: Hyperscript): List[Diff] = {
    val replacementsAndInserts = naiveDiff(a, b, ListBuffer())
    optimizeReplacementsAndInsertions(a, b, replacementsAndInserts)
  }

  def optimizeReplacementsAndInsertions(a: Hyperscript,
                                        b: Hyperscript,
                                        replacementsAndInserts: ListBuffer[Diff]): List[Diff] = {
    if (replacementsAndInserts.length > 1) {
      val first = replacementsAndInserts.headOption
      val second = replacementsAndInserts.tail.headOption
      (first, second) match {
        case (Some(Replacement(pathRep, elemRep)), Some(Insertion(pathIns, elemIns))) =>
          replacementsAndInserts.remove(0, 2)
          replacementsAndInserts.prepend(Insertion(pathRep, elemRep))
        case _ =>
      }
    }
    replacementsAndInserts.toList
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
                case (newNode, idx) => Insertion(currentPath :+ idx, newNode)
              })
            } else Nil
            first ++: ListBuffer(added: _*)
          } else {
            ListBuffer(Replacement(currentPath, bElem))
          }
        case _ => ListBuffer(Replacement(currentPath, b))
      }
    } else {
      ListBuffer()
    }
  }

  type Path = ListBuffer[Int]

  sealed trait Diff

  case class Replacement(selectChildren: Path, newNode: Hyperscript) extends Diff

  case class Insertion(selectInsertionNode: Path, newNode: Hyperscript) extends Diff

  object Path {
    def apply(indizes: Int*): Path = ListBuffer(indizes: _*)
  }

}


