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

jsDependencies ++= Seq(
  "org.webjars.npm" % "rxjs" % "5.0.0-beta.11" / "Rx.umd.min.js" commonJSName "Rx",
  RuntimeDOM
)

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
