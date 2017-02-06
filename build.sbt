import com.lihaoyi.workbench.Plugin.{bootSnippet, _}
import sbt.Keys.libraryDependencies

val commonSettings = workbenchSettings ++ Seq(
  organization := "com.campudus",
  scalaVersion := "2.12.1",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.github.lukajcb" %%% "rxscala-js" % "0.7.0",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ),
  bootSnippet := "tutorial.webapp.TutorialApp().main();",
  jsDependencies ++= Seq(
    "org.webjars.npm" % "rxjs" % "5.0.0-rc.1" / "bundles/Rx.min.js" commonJSName "Rx",
    RuntimeDOM
  ),
  updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
)

lazy val scycle = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "Scycle",
    version := "1.0.0"
  )

lazy val scycleExamples = (project in file("examples"))
  .dependsOn(scycle)
  .enablePlugins(ScalaJSPlugin)
  .aggregate(scycle)
  .settings(
    name := "Scycle Examples",
    version := "1.0.0"
  )
