package com.campudus.scycle.vdom

import com.campudus.scycle.dom.{Hyperscript, HyperscriptElement}
import org.scalajs.dom

import scala.collection.mutable.ListBuffer
import scala.util.Try

object VirtualDom {

  def apply(element: dom.Element): Hyperscript = element match {
    case null => null
    case HyperscriptElement(element) => element
  }

  def update(container: dom.Node, diffs: List[Diff]): Unit = diffs.foreach({
    case Replacement(ListBuffer(), null) =>
      do {
        container.removeChild(container.firstChild)
      } while(container.hasChildNodes())
    case Replacement(path, node) =>
      dom.console.log("find toReplace")
      val toReplace = path.foldLeft(container) {
        (subNode, childIdx) => subNode.childNodes(childIdx)
      }
      dom.console.log("found a toReplace:", toReplace)
      toReplace match {
        case currentElement: dom.Element =>
          val nodeElement = node.toElement
          val newAttributes = for {
            i <- 0 until nodeElement.attributes.length
          } yield nodeElement.attributes(i)

          newAttributes.filter(_.specified).foreach(attr => currentElement.setAttribute(attr.name, attr.value))
        case n: dom.Node =>
          n.parentNode.replaceChild(node.toElement, n)
      }
    case Insertion(path, node) =>
      if (path.nonEmpty) {
        val lastPath = path.last
        val parent = path.dropRight(1).foldLeft(container) {
          (subNode, childIdx) => subNode.childNodes(childIdx)
        }
        if (lastPath < parent.childNodes.length) {
          val elem = parent.childNodes(lastPath)
          parent.insertBefore(node.toElement, elem)
        } else {
          parent.appendChild(node.toElement)
        }
      } else {
        container.appendChild(node.toElement)
      }
  })

  def diff(a: Hyperscript, b: Hyperscript): List[Diff] = {
    if (a == null) {
      List(Insertion(Path(), b))
    } else if (b == null) {
      List(Replacement(Path(), null))
    } else {
      val replacementsAndInserts = naiveDiff(a, b, ListBuffer())
      optimizeReplacementsAndInsertions(a, b, replacementsAndInserts)
    }
  }

  def optimizeReplacementsAndInsertions(a: Hyperscript,
                                        b: Hyperscript,
                                        replacementsAndInserts: ListBuffer[Diff]): List[Diff] = {
    if (replacementsAndInserts.length > 1 && firstReplacementsCanBeInsertions(a, replacementsAndInserts)) {
      val first = replacementsAndInserts.head
      first match {
        case Replacement(pathRep, elemRep) =>
          replacementsAndInserts.zipWithIndex.find(_._1.isInstanceOf[Insertion]) match {
            case Some((Insertion(pathIns, elemIns), firstInsertIdx)) =>
              replacementsAndInserts.remove(0, firstInsertIdx + 1)
              replacementsAndInserts.prepend(Insertion(pathRep, elemRep))
            case _ => first :: optimizeReplacementsAndInsertions(a, b, replacementsAndInserts.tail)
          }
        case _ => first :: optimizeReplacementsAndInsertions(a, b, replacementsAndInserts.tail)
      }
    }
    replacementsAndInserts.toList
  }

  def firstReplacementsCanBeInsertions[T <: Diff](a: Hyperscript, list: ListBuffer[T]): Boolean = {
    if (list.exists(_.isInstanceOf[Insertion])) {
      val aElem = a.asInstanceOf[HyperscriptElement]
      val lastElements = aElem.subElements.reverse.zip(list.reverse).takeWhile({
        case (aChild, Diff(_, node)) => aChild == node
      })
      lastElements.nonEmpty || firstReplacementsCanBeInsertions(a, list.dropRight(1))
    } else {
      false
    }
  }

  def getElem(a: Hyperscript, path: Path): Hyperscript = a match {
    case a: HyperscriptElement =>
      if (path.nonEmpty) {
        val next = Try(a.subElements(path.head)).getOrElse(throw new NoSuchElementException)
        getElem(next, path.tail)
      } else a
    case _ => a
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

  sealed trait Diff {
    val path: Path
    val node: Hyperscript
  }

  case class Replacement(path: Path, node: Hyperscript) extends Diff

  case class Insertion(path: Path, node: Hyperscript) extends Diff

  object Diff {
    def unapply[T <: Diff](e: T): Option[(Path, Hyperscript)] = e match {
      case Replacement(p, n) => Some(p, n)
      case Insertion(p, n) => Some(p, n)
      case _ => None
    }
  }

  object Path {
    def apply(indizes: Int*): Path = ListBuffer(indizes: _*)
  }

}


