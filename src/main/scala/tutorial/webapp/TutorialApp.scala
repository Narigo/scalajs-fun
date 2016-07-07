package tutorial.webapp

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.{JSExport, JSExportAll}

@JSExport
object TutorialApp extends JSApp {

  @JSExport
  def main(): Unit = {
    dom.document.getElementById("hello").innerHTML = "test"
    println("Hello world!")
    val outerInt = 456
    val blubb = new Something(789)
    val result = blubb.testMethod()
    val method = () => blubb.testMethod()
    dom.console.log("blubb", result)
    dom.console.log("int", blubb.int)
    dom.console.log("boolean", blubb.bool)
    dom.console.log("test", blubb.test)
    dom.console.log("method", method())
  }

}

@JSExportAll
class Something(outerInt: Int) {
  val int: Int = 123
  val bool: Boolean = true
  val test: Int = outerInt

  def testMethod(): Int = 789 + test
}
