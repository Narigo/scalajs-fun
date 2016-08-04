import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "Scala.js Fun"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.8.0",
  "com.lihaoyi" %%% "scalarx" % "0.3.1",
  "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
)

bootSnippet := "tutorial.webapp.TutorialApp().main();"

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
