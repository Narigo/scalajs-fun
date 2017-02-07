import com.lihaoyi.workbench.Plugin.{bootSnippet, _}
import sbt.Keys.libraryDependencies

val commonSettings = Seq(
  organization := "com.campudus",
  scalaVersion := "2.12.1",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.github.lukajcb" %%% "rxscala-js" % "0.7.0",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ),
  jsDependencies ++= Seq(
    "org.webjars.npm" % "rxjs" % "5.0.0-rc.1" / "bundles/Rx.min.js" commonJSName "Rx",
    RuntimeDOM
  )
)

lazy val scycle = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "Scycle",
    version := "1.0.0"
  )

lazy val scycleExamples = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(scycle)
  .settings(commonSettings: _*)
  .settings(workbenchSettings: _*)
  .settings(
    name := "Scycle Examples",
    version := "1.0.0",
    updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile),
    bootSnippet := "com.campudus.scycle.examples.ScycleApp().main();"
  )
