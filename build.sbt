import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "Scala.js Fun"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.8.0",
  "com.lihaoyi" %%% "scalarx" % "0.2.8"
)

bootSnippet := "tutorial.webapp.TutorialApp().main();"

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
