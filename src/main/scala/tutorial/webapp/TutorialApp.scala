package tutorial.webapp

import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object TutorialApp extends JSApp {

  @JSExport
  def main(): Unit = {
    dom.document.getElementById("hello").innerHTML = "test"
    println("Hello world!")
    val blubb = new scala.scalajs.js.Object {
      val int: Int = 123
      val bool: Boolean = true

      def testMethod(): Int = 456
    }
    val result = blubb.testMethod()
    dom.console.log("test", blubb)
    dom.console.log("blubb", result)
  }

}
