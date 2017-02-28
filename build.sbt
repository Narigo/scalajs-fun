import com.lihaoyi.workbench.Plugin.{bootSnippet, _}
import sbt.Keys.libraryDependencies

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

val commonSettings = Seq(
  organization := "com.campudus",
  scalaVersion := "2.12.1",
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.github.lukajcb" %%% "rxscala-js" % "0.7.0",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ),
  jsDependencies ++= Seq(
    "org.webjars.npm" % "rxjs" % "5.0.0-rc.1" / "bundles/Rx.min.js" commonJSName "Rx",
    RuntimeDOM
  )
)

lazy val scycle = (project in file("scycle"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    commonSettings,
    name := "Scycle",
    version := "1.0.0"
  )

lazy val scycleExamples = (project in file("scycle-examples"))
  .dependsOn(scycle)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    commonSettings,
    workbenchSettings,
    name := "Scycle Examples",
    version := "1.0.0",
    scalaSource := baseDirectory.value / "src" / "examples" / "scala",
    resourceDirectory := baseDirectory.value / "src" / "examples" / "resources",
    updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile),
    bootSnippet := "com.campudus.scycle.examples.ScycleApp().main();"
  )
