package tutorial.webapp

import scala.scalajs.js.JSApp
import org.scalajs.dom
import scala.scalajs.js.annotation.JSExport

@JSExport
object TutorialApp extends JSApp {

  @JSExport
  def main(): Unit = {
    dom.document.getElementById("hello").innerHTML = "test"
    println("Hello world!")
  }

}
