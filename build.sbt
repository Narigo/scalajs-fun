import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "Scala.js Fun"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.github.lukajcb" %%% "rxscala-js" % "0.4.0",
  "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
)

bootSnippet := "tutorial.webapp.TutorialApp().main();"

jsDependencies += RuntimeDOM

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
